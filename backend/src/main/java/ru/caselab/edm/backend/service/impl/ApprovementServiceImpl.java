package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.approvementproccess.ApprovementProcessMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.ApprovementService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.VotingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.caselab.edm.backend.enums.ApprovementProcessStatus.PUBLISHED_FOR_VOTING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovementServiceImpl implements ApprovementService {
    private final UserRepository userRepository;
    private final ApprovementProcessRepository processRepository;
    private final ApprovementItemRepository itemRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ApprovementProcessMapper processMapper;
    private final VotingService votingService;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentService documentService;

    @Override
    public ApprovementProcessDTO createApprovementProcess(ApprovementProcessCreateDTO createProcess, UserInfoDetails authenticatedUser) {
        log.info("Started approval process for document version {}", createProcess.getDocumentId());

        DocumentVersion documentVersion = documentService.getLastVersionDocumentForUser(createProcess.getDocumentId(), authenticatedUser.getId());
        if (!documentVersion.getDocument().getUser().getId().equals(authenticatedUser.getId())) {
            throw new DocumentForbiddenAccess("You don't have access to this document with id = %d".formatted(createProcess.getDocumentId()));
        }
        documentVersion.getState().publishForVoting(documentVersion);
        ApprovementProcess process = buildApprovementProcess(createProcess, documentVersion);
        List<ApprovementProcessItem> processItems = createProcess.getUsersIds().stream().map(u -> createItem(u, documentVersion, process, authenticatedUser)).toList();
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

    private ApprovementProcessItem createItem(UUID userId, DocumentVersion documentVersion, ApprovementProcess process, UserInfoDetails authenticatedUser) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id = %s".formatted(userId));
        }
        User user = userOptional.get();
        ApprovementProcessItem approvementProcessItem = new ApprovementProcessItem();
        approvementProcessItem.setUser(user);
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


}
