package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.DelegationNotAvailableException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DelegationService;

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
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserPageDTO getAvailableUsersForDelegation(UUID userId, Long documentId, int size, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        DocumentVersion lastDocumentVersion = getLastVersionForDocument(documentId);
        if (user.getLeadDepartment() == null) {
            throw new DelegationNotAvailableException("This user has no root for delegation");
        }

        ApprovementProcessItem item = approvementItemRepository
                .findByDocumentVersionIdAndUserId(lastDocumentVersion.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Approvement item doesn't found"));

        Long departmentId = user.getLeadDepartment().getId();
        List<UUID> notAvailableUsers = new ArrayList<>();
        notAvailableUsers.add(userId);
        if (item.getApprovementProcess() != null) {
            ApprovementProcess process = approvementProcessRepository
                    .getApprovementProcessByDocumentVersion(lastDocumentVersion)
                    .orElseThrow(() -> new ResourceNotFoundException("Approvement process not found"));
            notAvailableUsers.addAll(process.getApprovementProcessItems().stream()
                    .map(processItem -> processItem.getUser().getId())
                    .toList());
        }

        return userMapper.toPageDTO(getUsersForDelegation(
                departmentId, PageRequest.of(page, size), notAvailableUsers));
    }

    @Transactional
    @Override
    public void delegateSign(UUID userIdToDelegate, UUID userIdFromDelegate, Long documentId) {
        log.info("Delegate sign from user {} to user {}", userIdFromDelegate, userIdToDelegate);
        DocumentVersion lastDocumentVersion = getLastVersionForDocument(documentId);
        ApprovementProcessItem item = approvementItemRepository
                .findByDocumentVersionIdAndUserId(lastDocumentVersion.getId(), userIdFromDelegate)
                .orElseThrow(() -> new ResourceNotFoundException("Process item for user not found "));

        User userFromDelegate = item.getUser();
        if (userFromDelegate.getLeadDepartment() == null) {
            throw new DelegationNotAvailableException("This user has no root for delegation");
        }

        Long departmentId = userFromDelegate.getLeadDepartment().getId();
        List<UUID> notAvailableUsers = new ArrayList<>();
        notAvailableUsers.add(userIdFromDelegate);
        if (item.getApprovementProcess() != null) {
            ApprovementProcess process = approvementProcessRepository
                    .getApprovementProcessByDocumentVersion(lastDocumentVersion)
                    .orElseThrow(() -> new ResourceNotFoundException("Approvement process not found"));
            notAvailableUsers.addAll(process.getApprovementProcessItems().stream()
                    .map(processItem -> processItem.getUser().getId())
                    .toList());
        }

        User user = userRepository
                .findUserByIdAndDepartmentAndNotInNotAvailableList(
                        userIdToDelegate, departmentId, notAvailableUsers)
                .orElseThrow(() -> new DelegationNotAvailableException(
                        "Forbidden to delegate sigh to this user"));

        item.setUser(user);
        approvementItemRepository.save(item);
    }

    private Page<User> getUsersForDelegation(Long departmentId, Pageable pageable, List<UUID> userIds) {
        return userRepository.getAvailableUsersForDelegation(departmentId, userIds, pageable);
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
