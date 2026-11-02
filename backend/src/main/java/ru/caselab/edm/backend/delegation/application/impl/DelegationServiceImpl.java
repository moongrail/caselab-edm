package ru.caselab.edm.backend.delegation.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementItemRepository;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementProcessRepository;
import ru.caselab.edm.backend.delegation.application.DelegationService;
import ru.caselab.edm.backend.delegation.exception.DelegationNotAvailableException;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserSummary;
import ru.caselab.edm.backend.shared.directory.api.UserInfoMapper;
import ru.caselab.edm.backend.shared.directory.api.UserInfoPageDTO;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelegationServiceImpl implements DelegationService {

    private final DocumentRepository documentRepository;
    private final ApprovementProcessRepository approvementProcessRepository;
    private final ApprovementItemRepository approvementItemRepository;
    private final UserDirectory userDirectory;

    @Transactional(readOnly = true)
    @Override
    public UserInfoPageDTO getAvailableUsersForDelegation(UUID userId, Long documentId,
                                                          int size, int page) {
        UserSummary user = userDirectory.requireUser(userId);
        DocumentVersion lastDocumentVersion = getLastVersionForDocument(documentId);
        if (user.managedDepartmentId() == null) {
            throw new DelegationNotAvailableException("This user has no root for delegation");
        }

        ApprovementProcessItem item = approvementItemRepository
                .findByDocumentVersionIdAndSignerId(lastDocumentVersion.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Approvement item doesn't found"));

        Long departmentId = user.managedDepartmentId();
        List<UUID> notAvailableUsers = new ArrayList<>();
        notAvailableUsers.add(userId);
        if (item.getApprovementProcess() != null) {
            ApprovementProcess process = approvementProcessRepository
                    .getApprovementProcessByDocumentVersion(lastDocumentVersion)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Approvement process not found"));
            notAvailableUsers.addAll(process.getApprovementProcessItems().stream()
                    .map(ApprovementProcessItem::getSignerId)
                    .toList());
        }

        return UserInfoMapper.toPage(userDirectory.departmentMembers(
                departmentId, notAvailableUsers, page, size));
    }

    @Transactional
    @Override
    public void delegateSign(UUID userIdToDelegate, UUID userIdFromDelegate, Long documentId) {
        log.info("Delegate sign from user {} to user {}", userIdFromDelegate, userIdToDelegate);
        DocumentVersion lastDocumentVersion = getLastVersionForDocument(documentId);
        ApprovementProcessItem item = approvementItemRepository
                .findByDocumentVersionIdAndSignerId(lastDocumentVersion.getId(), userIdFromDelegate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Process item for user not found "));

        UserSummary userFromDelegate = userDirectory.requireUser(item.getSignerId());
        if (userFromDelegate.managedDepartmentId() == null) {
            throw new DelegationNotAvailableException("This user has no root for delegation");
        }

        Long departmentId = userFromDelegate.managedDepartmentId();
        List<UUID> notAvailableUsers = new ArrayList<>();
        notAvailableUsers.add(userIdFromDelegate);
        if (item.getApprovementProcess() != null) {
            ApprovementProcess process = approvementProcessRepository
                    .getApprovementProcessByDocumentVersion(lastDocumentVersion)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Approvement process not found"));
            notAvailableUsers.addAll(process.getApprovementProcessItems().stream()
                    .map(ApprovementProcessItem::getSignerId)
                    .toList());
        }

        UserSummary target = userDirectory.requireUser(userIdToDelegate);
        boolean sameDepartment = departmentId.equals(target.departmentId());
        boolean unavailable = notAvailableUsers.contains(userIdToDelegate);
        if (!sameDepartment || unavailable) {
            throw new DelegationNotAvailableException(
                    "Forbidden to delegate sigh to this user");
        }

        item.setSignerId(target.id());
        approvementItemRepository.save(item);
    }

    private DocumentVersion getLastVersionForDocument(Long documentId) {
        log.info("Get document with id: {} ", documentId);
        Document document = documentRepository.getDocumentById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        log.info("Get last version document");
        return document.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt)
                        .thenComparing(DocumentVersion::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResourceNotFoundException("Document Version not found"));
    }
}
