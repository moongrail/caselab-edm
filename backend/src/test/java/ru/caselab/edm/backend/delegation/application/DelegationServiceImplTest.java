package ru.caselab.edm.backend.delegation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessStatus;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementItemRepository;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementProcessRepository;
import ru.caselab.edm.backend.delegation.application.impl.DelegationServiceImpl;
import ru.caselab.edm.backend.delegation.exception.DelegationNotAvailableException;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserPage;
import ru.caselab.edm.backend.shared.directory.UserSummary;
import ru.caselab.edm.backend.shared.directory.api.UserInfoPageDTO;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.THIRD_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.approvementProcess;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.manager;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class DelegationServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ApprovementProcessRepository processRepository;
    @Mock
    private ApprovementItemRepository itemRepository;
    @Mock
    private UserDirectory userDirectory;

    @InjectMocks
    private DelegationServiceImpl delegationService;

    private UserSummary owner;
    private UserSummary signer;
    private Document document;
    private DocumentVersion version;

    @BeforeEach
    void setUp() {
        owner = manager(OWNER_ID, "owner", 5L);
        signer = user(SIGNER_ID, "signer");
        document = document(DOCUMENT_ID, OWNER_ID, documentType(1L));
        version = version(VERSION_ID, document, DocumentStatus.DRAFT);
        lenient().when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.of(document));
    }

    @Test
    void getAvailableUsersForDelegation_withoutProcess_returnsDepartmentUsers() {
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, version, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userDirectory.departmentMembers(eq(5L), anyCollection(), eq(1), eq(10)))
                .thenReturn(new UserPage(List.of(signer), 1, 10, 1, 1));

        UserInfoPageDTO result = delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 1);

        assertThat(result.content()).extracting("id").containsExactly(SIGNER_ID);
        assertThat(result.number()).isEqualTo(1);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> excludedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userDirectory).departmentMembers(eq(5L), excludedCaptor.capture(), eq(1), eq(10));
        assertThat(excludedCaptor.getValue()).containsExactly(OWNER_ID);
    }

    @Test
    void getAvailableUsersForDelegation_withProcessExcludesAllParticipants() {
        ApprovementProcess process = approvementProcess(
                10L, version, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        process.getApprovementProcessItems().add(approvementItem(
                1L, SIGNER_ID, version, process,
                ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN));
        ApprovementProcessItem item = approvementItem(
                2L, OWNER_ID, version, process,
                ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(processRepository.getApprovementProcessByDocumentVersion(version))
                .thenReturn(Optional.of(process));
        when(userDirectory.departmentMembers(eq(5L), anyCollection(), eq(0), eq(10)))
                .thenReturn(new UserPage(List.of(), 0, 10, 0, 0));

        delegationService.getAvailableUsersForDelegation(OWNER_ID, DOCUMENT_ID, 10, 0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> excludedCaptor = ArgumentCaptor.forClass(List.class);
        verify(userDirectory).departmentMembers(eq(5L), excludedCaptor.capture(), eq(0), eq(10));
        assertThat(excludedCaptor.getValue()).containsExactly(OWNER_ID, SIGNER_ID);
    }

    @Test
    void getAvailableUsersForDelegation_missingUser_throws() {
        when(userDirectory.requireUser(OWNER_ID))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getAvailableUsersForDelegation_withoutManagedDepartment_throws() {
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(user(OWNER_ID, "owner"));

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(DelegationNotAvailableException.class)
                .hasMessageContaining("no root");
    }

    @Test
    void getAvailableUsersForDelegation_missingDocument_throws() {
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    @Test
    void getAvailableUsersForDelegation_documentWithoutVersions_throws() {
        Document emptyDocument = document(DOCUMENT_ID, OWNER_ID, documentType(1L));
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(documentRepository.getDocumentById(DOCUMENT_ID)).thenReturn(Optional.of(emptyDocument));

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document Version not found");
    }

    @Test
    void getAvailableUsersForDelegation_missingApprovementItem_throws() {
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.getAvailableUsersForDelegation(
                OWNER_ID, DOCUMENT_ID, 10, 0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approvement item");
    }

    @Test
    void delegateSign_withoutProcess_reassignsSigner() {
        UserSummary target = manager(THIRD_ID, "target", 5L);
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, version, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(userDirectory.requireUser(THIRD_ID)).thenReturn(target);

        delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID);

        assertThat(item.getSignerId()).isEqualTo(THIRD_ID);
        verify(itemRepository).save(item);
    }

    @Test
    void delegateSign_withProcess_excludesProcessParticipants() {
        UserSummary target = manager(THIRD_ID, "target", 5L);
        ApprovementProcess process = approvementProcess(
                10L, version, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        process.getApprovementProcessItems().add(approvementItem(
                2L, SIGNER_ID, version, process,
                ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN));
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, version, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(processRepository.getApprovementProcessByDocumentVersion(version))
                .thenReturn(Optional.of(process));
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(userDirectory.requireUser(THIRD_ID)).thenReturn(target);

        delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID);

        assertThat(item.getSignerId()).isEqualTo(THIRD_ID);
    }

    @Test
    void delegateSign_targetFromAnotherDepartment_throws() {
        UserSummary target = user(THIRD_ID, "target");
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, version, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);
        when(userDirectory.requireUser(THIRD_ID)).thenReturn(target);

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(DelegationNotAvailableException.class)
                .hasMessageContaining("Forbidden");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void delegateSign_missingItem_throws() {
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Process item");
    }

    @Test
    void delegateSign_senderWithoutManagedDepartment_throws() {
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, version, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(user(OWNER_ID, "owner"));

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
                10L, version, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, version, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(itemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(processRepository.getApprovementProcessByDocumentVersion(version))
                .thenReturn(Optional.empty());
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(owner);

        assertThatThrownBy(() -> delegationService.delegateSign(THIRD_ID, OWNER_ID, DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approvement process");
    }
}
