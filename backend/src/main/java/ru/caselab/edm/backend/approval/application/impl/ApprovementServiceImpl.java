package ru.caselab.edm.backend.approval.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.delegation.domain.ReplacementManager;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserSummary;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.approval.domain.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.backend.approval.api.mapper.ApprovementProcessMapper;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementItemRepository;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementProcessRepository;
import ru.caselab.edm.backend.delegation.infrastructure.ReplacementManagerRepository;
import ru.caselab.edm.backend.approval.application.ApprovementService;
import ru.caselab.edm.backend.document.application.AuthorizationService;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.backend.document.application.DocumentVersionService;
import ru.caselab.edm.backend.approval.application.VotingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.caselab.edm.backend.approval.domain.ApprovementProcessStatus.PUBLISHED_FOR_VOTING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovementServiceImpl implements ApprovementService {
    private final UserDirectory userDirectory;
    private final ReplacementManagerRepository replacementManagerRepository;
    private final ApprovementProcessRepository processRepository;
    private final ApprovementItemRepository itemRepository;
    private final ApprovementProcessMapper processMapper;
    private final VotingService votingService;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;
    private final AuthorizationService authorizationService;

    @Transactional
    @Override
    public ApprovementProcessDTO createApprovementProcess(ApprovementProcessCreateDTO createProcess, AuthenticatedUser authenticatedUser) {
        log.info("Started approval process for document {}", createProcess.getDocumentId());

        DocumentVersion documentVersion = documentService.getLastVersionDocumentForUser(createProcess.getDocumentId(), authenticatedUser.id());
        authorizationService.checkDocumentOwner(documentVersion.getDocument(), authenticatedUser.id());
        documentVersion.getStateMachine().publishForVoting(documentVersion);
        ApprovementProcess process = buildApprovementProcess(createProcess, documentVersion);
        List<UUID> usersForApproval = findUsersForApprovementProcess(createProcess.getUsersIds());
        List<ApprovementProcessItem> processItems = usersForApproval.stream()
                .map(userId -> createItem(userId, documentVersion, process))
                .toList();
        process.getApprovementProcessItems().clear();
        process.getApprovementProcessItems().addAll(processItems);
        documentVersion.setApprovementProcesses(
                documentVersion.getApprovementProcesses() != null
                        ? documentVersion.getApprovementProcesses()
                        : new ArrayList<>()
        );
        documentVersion.getApprovementProcesses().add(process);
        //проверка, можем ли мы опубликовать документ на голосование
        processRepository.save(process);
        votingService.scheduleVotingJob(process.getId(), process.getDeadline());

        return processMapper.toDTO(process);
    }

    @Transactional(readOnly = true)
    @Override
    public ApprovementProcessResultDTO resultOfApprovementProcess(Long documentId) {
        log.info("Result of approval process for document {}", documentId);
        DocumentVersion version = documentVersionService.getDocumentVersion(documentId);
        ApprovementProcess process = processRepository.getApprovementProcessByDocumentVersion(version)
                .orElseThrow(()-> new ResourceNotFoundException("Approvement process not found for document id = %s".formatted(documentId)));
        return processMapper.toResultDTO(process);
    }

    private ApprovementProcessItem createItem(UUID signerId, DocumentVersion documentVersion, ApprovementProcess process) {
        ApprovementProcessItem approvementProcessItem = new ApprovementProcessItem();
        approvementProcessItem.setSignerId(signerId);
        approvementProcessItem.setDocumentVersion(documentVersion);
        approvementProcessItem.setStatus(ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        approvementProcessItem.setCreatedAt(LocalDateTime.now());
        approvementProcessItem.setApprovementProcess(process);
        itemRepository.save(approvementProcessItem);
        eventPublisher.publishEvent(new DocumentSignRequestEvent(this, approvementProcessItem));
        return approvementProcessItem;
    }

    private ApprovementProcess buildApprovementProcess(ApprovementProcessCreateDTO createProcess, DocumentVersion version) {
        ApprovementProcess process = new ApprovementProcess();
        process.setAgreementProcent(createProcess.getAgreementPercent());
        process.setStatus(PUBLISHED_FOR_VOTING);
        process.setDocumentVersion(version);
        process.setDeadline(createProcess.getDeadline());


        return processRepository.save(process);
    }

    private List<UUID> findUsersForApprovementProcess(Set<UUID> userIds) {
        Map<UUID, UUID> replacements = findReplacementsForUsers(userIds);
        LinkedHashSet<UUID> effectiveIds = new LinkedHashSet<>();
        userIds.forEach(userId -> effectiveIds.add(replacements.getOrDefault(userId, userId)));

        List<UserSummary> found = userDirectory.findUsers(effectiveIds);
        if (found.size() != effectiveIds.size()) {
            Set<UUID> foundIds = found.stream().map(UserSummary::id).collect(Collectors.toSet());
            UUID missing = effectiveIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new ResourceNotFoundException(
                    "User not found with id = %s".formatted(missing));
        }
        return new ArrayList<>(effectiveIds);
    }

    private Map<UUID, UUID> findReplacementsForUsers(Set<UUID> userIds) {
        return replacementManagerRepository.findActiveReplacementsByManagerUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        ReplacementManager::getManagerUserId,
                        ReplacementManager::getTempManagerUserId,
                        (existing, replacement) -> existing));
    }
}
