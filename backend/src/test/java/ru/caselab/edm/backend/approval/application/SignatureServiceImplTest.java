package ru.caselab.edm.backend.approval.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.approval.api.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.backend.approval.domain.Signature;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessStatus;
import ru.caselab.edm.backend.document.exception.InvalidDocumentStateException;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.backend.approval.exception.SignatureAlreadyExistsException;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementItemRepository;
import ru.caselab.edm.backend.approval.infrastructure.SignatureRepository;
import ru.caselab.edm.backend.approval.application.impl.SignatureServiceImpl;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;
import ru.caselab.edm.platform.security.AuthenticatedUser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.approvementProcess;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.signature;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class SignatureServiceImplTest {

    @Mock
    private SignatureRepository signatureRepository;

    @Mock
    private ApprovementItemRepository approvementItemRepository;

    @Mock
    private DocumentService documentService;

    private AuthenticatedUser authenticatedUser;

    @InjectMocks
    private SignatureServiceImpl signatureService;

    private Document document;
    private DocumentVersion documentVersion;

    @BeforeEach
    void setUp() {
        authenticatedUser = new AuthenticatedUser(OWNER_ID, "owner", java.util.Set.of("USER"), null, null);
        document = document(DOCUMENT_ID, OWNER_ID, documentType(1L));
        documentVersion = version(VERSION_ID, document, DocumentStatus.PENDING_AUTHOR_SIGN);

        lenient().when(documentService.getLastVersionDocumentWhereUserSignatories(DOCUMENT_ID, OWNER_ID))
                .thenReturn(documentVersion);
    }

    @Test
    void sign_authorSignature_movesDocumentToAuthorSignedAndStoresSignature() {
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));

        signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser);

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.AUTHOR_SIGNED);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.APPROVED);
        verify(approvementItemRepository).save(item);

        ArgumentCaptor<Signature> signatureCaptor = ArgumentCaptor.forClass(Signature.class);
        verify(signatureRepository).save(signatureCaptor.capture());
        Signature savedSignature = signatureCaptor.getValue();
        assertThat(savedSignature.getApprovementProcessItem()).isSameAs(item);
        assertThat(savedSignature.getCreatedAt()).isNotNull();
        assertThat(savedSignature.getHash()).isEqualTo(expectedHash(OWNER_ID, VERSION_ID));
    }

    @Test
    void sign_authorSignatureInVoting_doesNotChangeDocumentStatus() {
        documentVersion.setStatus(DocumentStatus.IN_VOTING);
        ApprovementProcess process = approvementProcess(
                5L, documentVersion, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        documentVersion.getApprovementProcesses().add(process);
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, documentVersion, process, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));

        signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser);

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.IN_VOTING);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.APPROVED);
        verify(signatureRepository).save(any(Signature.class));
    }

    @Test
    void sign_authorSignatureWhenVotingAlreadyCompleted_throwsWithoutSaving() {
        documentVersion.setStatus(DocumentStatus.IN_VOTING);
        ApprovementProcess process = approvementProcess(
                5L, documentVersion, ApprovementProcessStatus.VOTING_COMPLETED);
        documentVersion.getApprovementProcesses().add(process);
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, documentVersion, process, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(InvalidDocumentStateException.class);

        verify(approvementItemRepository, never()).save(any());
        verify(signatureRepository, never()).save(any());
    }

    @Test
    void sign_contractorApproves_movesDocumentToApproved() {
        authenticateAsSigner();
        documentVersion.setStatus(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        ApprovementProcessItem item = approvementItem(
                1L, SIGNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser);

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.APPROVED);
        verify(signatureRepository).save(any(Signature.class));
    }

    @Test
    void sign_contractorRejects_movesDocumentToRejected() {
        authenticateAsSigner();
        documentVersion.setStatus(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        ApprovementProcessItem item = approvementItem(
                1L, SIGNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        signatureService.sign(signature("REJECTED"), DOCUMENT_ID, authenticatedUser);

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.REJECTED);
    }

    @Test
    void sign_contractorRequestsRework_movesDocumentToReworkRequired() {
        authenticateAsSigner();
        documentVersion.setStatus(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        ApprovementProcessItem item = approvementItem(
                1L, SIGNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        signatureService.sign(signature("REWORK_REQUIRED"), DOCUMENT_ID, authenticatedUser);

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.REWORK_REQUIRED);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.REWORK_REQUIRED);
    }

    @Test
    void sign_contractorInVoting_keepsVersionStatusAndStoresVote() {
        authenticateAsSigner();
        documentVersion.setStatus(DocumentStatus.IN_VOTING);
        ApprovementProcess process = approvementProcess(
                5L, documentVersion, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        ApprovementProcessItem item = approvementItem(
                1L, SIGNER_ID, documentVersion, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        signatureService.sign(signature("REJECTED"), DOCUMENT_ID, authenticatedUser);

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.IN_VOTING);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.REJECTED);
        verify(signatureRepository).save(any(Signature.class));
    }

    @Test
    void sign_contractorInCompletedVoting_throwsWithoutSaving() {
        authenticateAsSigner();
        documentVersion.setStatus(DocumentStatus.IN_VOTING);
        ApprovementProcess process = approvementProcess(
                5L, documentVersion, ApprovementProcessStatus.VOTING_COMPLETED);
        ApprovementProcessItem item = approvementItem(
                1L, SIGNER_ID, documentVersion, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(InvalidDocumentStateException.class);

        verify(approvementItemRepository, never()).save(any());
        verify(signatureRepository, never()).save(any());
    }

    @Test
    void sign_alreadySigned_throwsAndDoesNotChangeState() {
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));
        when(signatureRepository.existsByApprovementProcessItemId(1L)).thenReturn(true);

        assertThatThrownBy(() -> signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(SignatureAlreadyExistsException.class)
                .hasMessageContaining("already been signed");

        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.PENDING_AUTHOR_SIGN);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        verify(approvementItemRepository, never()).save(any());
        verify(signatureRepository, never()).save(any());
    }

    @Test
    void sign_invalidSigningRequest_throwsResourceNotFound() {
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.empty());
        SignatureCreateDTO createDTO = signature("APPROVED");

        assertThatThrownBy(() -> signatureService.sign(createDTO, DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Signing request not found");
    }

    @Test
    void sign_invalidStateForAuthor_throwsWithoutSaving() {
        documentVersion.setStatus(DocumentStatus.APPROVED);
        ApprovementProcessItem item = approvementItem(
                1L, OWNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, OWNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(InvalidDocumentStateException.class);
        verify(signatureRepository, never()).save(any());
    }

    @Test
    void sign_invalidContractorStatus_throwsWithoutSaving() {
        authenticateAsSigner();
        documentVersion.setStatus(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        ApprovementProcessItem item = approvementItem(
                1L, SIGNER_ID, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndSignerId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> signatureService.sign(
                signature("PENDING_CONTRACTOR_SIGN"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(InvalidDocumentStateException.class);

        verify(approvementItemRepository, never()).save(any());
        verify(signatureRepository, never()).save(any());
    }

    private void authenticateAsSigner() {
        authenticatedUser = new AuthenticatedUser(SIGNER_ID, "signer", java.util.Set.of("USER"), null, null);
        when(documentService.getLastVersionDocumentWhereUserSignatories(DOCUMENT_ID, SIGNER_ID))
                .thenReturn(documentVersion);
    }

    private String expectedHash(java.util.UUID userId, Long versionId) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((userId.toString() + versionId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
