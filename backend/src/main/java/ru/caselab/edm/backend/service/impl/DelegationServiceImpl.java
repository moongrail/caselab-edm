package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.*;
import ru.caselab.edm.backend.exceptions.DelegationNotAvailableException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DelegationService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelegationServiceImpl implements DelegationService {

    private final DocumentRepository documentRepository;
    private final ApprovementProcessRepository approvementProcessRepository;
    private final ApprovementItemRepository approvementItemRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserPageDTO getAvailableUsersForDelegation(UUID userId, Long documentId, int size, int page) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        DocumentVersion lastDocumentVersion = getLastVersionForDocument(documentId);
        if(user.getLeadDepartment()==null)
        {throw new DelegationNotAvailableException("This user has no root for delegation"); }
        Optional<ApprovementProcessItem> optionalItem = approvementItemRepository.findByDocumentVersionIdAndUserId(lastDocumentVersion.getId(), userId);
        if(optionalItem.isPresent()){
            ApprovementProcessItem item = optionalItem.get();
            List<UUID> notAvailableUsersForDelegation = new ArrayList<>();
            Long departmentId = user.getLeadDepartment().getId();
            notAvailableUsersForDelegation.add(userId);
            if(item.getApprovementProcess()==null){
                return userMapper.toPageDTO(getUsersForDelegation(departmentId,PageRequest.of(page,size),notAvailableUsersForDelegation));
            }else{
                ApprovementProcess process = approvementProcessRepository.getApprovementProcessByDocumentVersion(lastDocumentVersion).get();
                notAvailableUsersForDelegation.addAll(process.getApprovementProcessItems().stream().map(i->i.getUser().getId()).toList());
                return userMapper.toPageDTO(getUsersForDelegation(departmentId,PageRequest.of(page,size),notAvailableUsersForDelegation));
            }
        }else{
            throw new ResourceNotFoundException("Approvement item doesn't found");
        }

    }

    @Override
    public void delegateSign(UUID userIdToDelegate,UUID userIdFromDelegate, Long documentId) {
        log.info("Delegate sign from user {} to user {}",userIdFromDelegate,userIdToDelegate);
        DocumentVersion lastDocumentVersion = getLastVersionForDocument(documentId);
        ApprovementProcessItem item = approvementItemRepository.findByDocumentVersionIdAndUserId(lastDocumentVersion.getId(), userIdFromDelegate)
                .orElseThrow(() -> new ResourceNotFoundException("Process item for user not found "));

        User userFromDelegate = item.getUser();
        if(userFromDelegate.getLeadDepartment()==null) {throw new DelegationNotAvailableException("This user has no root for delegation"); }

        Long departmentId = userFromDelegate.getLeadDepartment().getId();
        List<UUID> notAvailableUsersForDelegation = new ArrayList<>();
        notAvailableUsersForDelegation.add(userIdFromDelegate);
        if(item.getApprovementProcess()!=null) {
            ApprovementProcess process = approvementProcessRepository.getApprovementProcessByDocumentVersion(lastDocumentVersion).get();
            notAvailableUsersForDelegation.addAll(process.getApprovementProcessItems().stream().map(i->i.getUser().getId()).toList());
        }

        User user = userRepository.findUserByIdAndDepartmentAndNotInNotAvailableList(userIdToDelegate,departmentId,notAvailableUsersForDelegation)
                .orElseThrow(() -> new DelegationNotAvailableException("Forbidden to delegate sigh to this user"));

        item.setUser(user);
        approvementItemRepository.save(item);
    }

    private Page<User> getUsersForDelegation(Long departmentId, Pageable pageable, List<UUID> userIds  ) {
        return userRepository.getAvailableUsersForDelegation(departmentId,userIds, pageable);
    }

    private DocumentVersion getLastVersionForDocument(Long documentId){
        log.info("Get document with id: {} ", documentId);
        Document document = documentRepository.getDocumentById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        log.info("Get last version document");
        return document.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow(()->new ResourceNotFoundException("Document Version not found"));
    }
}
