package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.Signature;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;
import ru.caselab.edm.backend.exceptions.InvalidDocumentStateException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.SignatureAlreadyExistsException;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.SignatureRepository;
import ru.caselab.edm.backend.service.impl.SignatureServiceImpl;
import ru.caselab.edm.backend.state.DocumentStatus;

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
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementProcess;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.signature;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class SignatureServiceImplTest {

    @Mock
    private SignatureRepository signatureRepository;

    @Mock
    private ApprovementItemRepository approvementItemRepository;

    @Mock
    private DocumentService documentService;

    @Mock
    private UserInfoDetails authenticatedUser;

    @InjectMocks
    private SignatureServiceImpl signatureService;

    private User owner;
    private User signer;
    private Document document;
    private DocumentVersion documentVersion;

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID, "owner");
        signer = user(SIGNER_ID, "signer");
        document = document(DOCUMENT_ID, owner, documentType(1L));
        documentVersion = version(VERSION_ID, document, DocumentStatus.PENDING_AUTHOR_SIGN);

        lenient().when(authenticatedUser.getId()).thenReturn(OWNER_ID);
        lenient().when(documentService.getLastVersionDocumentWhereUserSignatories(DOCUMENT_ID, OWNER_ID))
                .thenReturn(documentVersion);
    }

    @Test
    void sign_authorSignature_movesDocumentToAuthorSignedAndStoresSignature() {
        ApprovementProcessItem item = approvementItem(
                1L, owner, documentVersion, null, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
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
                1L, owner, documentVersion, process, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
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
                1L, owner, documentVersion, process, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
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
                1L, signer, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
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
                1L, signer, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
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
                1L, signer, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
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
                1L, signer, documentVersion, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
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
                1L, signer, documentVersion, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> signatureService.sign(signature("APPROVED"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(InvalidDocumentStateException.class);

        verify(approvementItemRepository, never()).save(any());
        verify(signatureRepository, never()).save(any());
    }

    @Test
    void sign_alreadySigned_throwsAndDoesNotChangeState() {
        ApprovementProcessItem item = approvementItem(
                1L, owner, documentVersion, null, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
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
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
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
                1L, owner, documentVersion, null, ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, OWNER_ID))
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
                1L, signer, documentVersion, null, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
        when(approvementItemRepository.findByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> signatureService.sign(
                signature("PENDING_CONTRACTOR_SIGN"), DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(InvalidDocumentStateException.class);

        verify(approvementItemRepository, never()).save(any());
        verify(signatureRepository, never()).save(any());
    }

    private void authenticateAsSigner() {
        when(authenticatedUser.getId()).thenReturn(SIGNER_ID);
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
