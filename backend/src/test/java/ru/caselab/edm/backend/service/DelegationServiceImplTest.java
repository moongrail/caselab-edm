package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Department;
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
import ru.caselab.edm.backend.service.impl.DelegationServiceImpl;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.THIRD_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementProcess;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class DelegationServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ApprovementProcessRepository processRepository;
    @Mock
    private ApprovementItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private DelegationServiceImpl delegationService;

    private User owner;
    private User signer;
    private Department department;
    private Document document;
    private DocumentVersion version;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(5L);
        department.setName("Department");

        owner = user(OWNER_ID, "owner");
        owner.setLeadDepartment(department);
        signer = user(SIGNER_ID, "signer");
        signer.setLeadDepartment(department);
        document = document(DOCUMENT_ID, owner, documentType(1L));
        version = version(VERSION_ID, document, DocumentStatus.DRAFT);
        lenient().when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.of(document));
    }

    @Test
    void getAvailableUsersForDelegation_withoutProcess_returnsDepartmentUsers() {
        Page<User> users = new PageImpl<>(List.of(signer));
        UserPageDTO expected = new UserPageDTO(0, 0, 0, 0, List.of());
        ApprovementProcessItem item = approvementItem(
                1L, owner, version, null, ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userRepository.getAvailableUsersForDelegation(
                eq(5L), anyList(), any(Pageable.class))).thenReturn(users);
        when(userMapper.toPageDTO(users)).thenReturn(expected);

        UserPageDTO result = delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 1);

        assertThat(result).isSameAs(expected);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<java.util.UUID>> excludedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).getAvailableUsersForDelegation(
                eq(5L), excludedCaptor.capture(), eq(PageRequest.of(1, 10)));
        assertThat(excludedCaptor.getValue()).containsExactly(OWNER_ID);
    }

    @Test
    void getAvailableUsersForDelegation_withProcessExcludesAllParticipants() {
        Page<User> users = new PageImpl<>(List.of());
        ApprovementProcess process = approvementProcess(
                10L, version, ru.caselab.edm.backend.enums.ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        process.getApprovementProcessItems().add(approvementItem(
                1L, signer, version, process,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN));
        ApprovementProcessItem item = approvementItem(
                2L, owner, version, process,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(processRepository.getApprovementProcessByDocumentVersion(version))
                .thenReturn(Optional.of(process));
        when(userRepository.getAvailableUsersForDelegation(
                eq(5L), anyList(), any(Pageable.class))).thenReturn(users);
        when(userMapper.toPageDTO(users)).thenReturn(new UserPageDTO(0, 0, 0, 0, List.of()));

        delegationService.getAvailableUsersForDelegation(OWNER_ID, DOCUMENT_ID, 10, 0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<java.util.UUID>> excludedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).getAvailableUsersForDelegation(
                eq(5L), excludedCaptor.capture(), eq(PageRequest.of(0, 10)));
        assertThat(excludedCaptor.getValue()).containsExactly(OWNER_ID, SIGNER_ID);
    }

    @Test
    void getAvailableUsersForDelegation_missingUser_throws() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getAvailableUsersForDelegation_withoutLeadDepartment_throws() {
        owner.setLeadDepartment(null);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(DelegationNotAvailableException.class)
                .hasMessageContaining("no root");
    }

    @Test
    void getAvailableUsersForDelegation_missingDocument_throws() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    @Test
    void getAvailableUsersForDelegation_documentWithoutVersions_throws() {
        Document emptyDocument = document(DOCUMENT_ID, owner, documentType(1L));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.of(emptyDocument));

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document Version not found");
    }

    @Test
    void getAvailableUsersForDelegation_missingApprovementItem_throws() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approvement item");
    }

    @Test
    void delegateSign_withoutProcess_reassignsSigner() {
        User target = user(THIRD_ID, "target");
        ApprovementProcessItem item = approvementItem(
                1L, owner, version, null,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userRepository.findUserByIdAndDepartmentAndNotInNotAvailableList(
                eq(THIRD_ID), eq(5L), anyList())).thenReturn(Optional.of(target));

        delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID);

        assertThat(item.getUser()).isSameAs(target);
        verify(itemRepository).save(item);
    }

    @Test
    void delegateSign_withProcess_excludesProcessParticipants() {
        User target = user(THIRD_ID, "target");
        ApprovementProcess process = approvementProcess(
                10L, version, ru.caselab.edm.backend.enums.ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        process.getApprovementProcessItems().add(approvementItem(
                2L, signer, version, process,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN));
        ApprovementProcessItem item = approvementItem(
                1L, owner, version, process,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(processRepository.getApprovementProcessByDocumentVersion(version))
                .thenReturn(Optional.of(process));
        when(userRepository.findUserByIdAndDepartmentAndNotInNotAvailableList(
                eq(THIRD_ID), eq(5L), anyList())).thenReturn(Optional.of(target));

        delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<java.util.UUID>> excludedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).findUserByIdAndDepartmentAndNotInNotAvailableList(
                eq(THIRD_ID), eq(5L), excludedCaptor.capture());
        assertThat(excludedCaptor.getValue()).containsExactly(OWNER_ID, SIGNER_ID);
        assertThat(item.getUser()).isSameAs(target);
    }

    @Test
    void delegateSign_missingTarget_throws() {
        ApprovementProcessItem item = approvementItem(
                1L, owner, version, null,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userRepository.findUserByIdAndDepartmentAndNotInNotAvailableList(
                eq(THIRD_ID), eq(5L), anyList())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(DelegationNotAvailableException.class)
                .hasMessageContaining("Forbidden");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void delegateSign_missingItem_throws() {
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Process item");
    }

    @Test
    void delegateSign_senderWithoutLeadDepartment_throws() {
        owner.setLeadDepartment(null);
        ApprovementProcessItem item = approvementItem(
                1L, owner, version, null,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(DelegationNotAvailableException.class)
                .hasMessageContaining("no root");
    }

    @Test
    void delegateSign_missingDocument_throws() {
        when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    @Test
    void delegateSign_withProcessMissingInRepository_throwsInsteadOfNpe() {
        ApprovementProcess process = approvementProcess(
                10L, version, ru.caselab.edm.backend.enums.ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        ApprovementProcessItem item = approvementItem(
                1L, owner, version, process,
                ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(processRepository.getApprovementProcessByDocumentVersion(version))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approvement process");
    }
}
