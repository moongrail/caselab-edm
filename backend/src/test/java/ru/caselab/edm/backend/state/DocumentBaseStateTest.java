package ru.caselab.edm.backend.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;
import ru.caselab.edm.backend.exceptions.InvalidDocumentStateException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementProcess;

class DocumentBaseStateTest {

    private final DocumentState state = DocumentBaseState.INSTANCE;

    @Test
    void signAuthor_withoutVoting_movesDraftToAuthorSigned() {
        DocumentVersion version = version(DocumentStatus.DRAFT);

        state.signAuthor(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.AUTHOR_SIGNED);
    }

    @Test
    void signAuthor_withoutVoting_movesPendingAuthorSignToAuthorSigned() {
        DocumentVersion version = version(DocumentStatus.PENDING_AUTHOR_SIGN);

        state.signAuthor(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.AUTHOR_SIGNED);
    }

    @Test
    void signAuthor_withVotingInProgress_keepsStatus() {
        DocumentVersion version = version(DocumentStatus.IN_VOTING);
        version.getApprovementProcesses().add(process(ApprovementProcessStatus.PUBLISHED_FOR_VOTING));

        state.signAuthor(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.IN_VOTING);
    }

    @Test
    void signAuthor_withCompletedVoting_throws() {
        DocumentVersion version = version(DocumentStatus.IN_VOTING);
        version.getApprovementProcesses().add(process(ApprovementProcessStatus.VOTING_COMPLETED));

        assertThatThrownBy(() -> state.signAuthor(version))
                .isInstanceOf(InvalidDocumentStateException.class)
                .hasMessageContaining("voting");
    }

    @Test
    void signAuthor_fromApproved_throws() {
        DocumentVersion version = version(DocumentStatus.APPROVED);

        assertThatThrownBy(() -> state.signAuthor(version))
                .isInstanceOf(InvalidDocumentStateException.class)
                .hasMessageContaining("APPROVED");
    }

    @ParameterizedTest
    @EnumSource(value = ApprovementProcessItemStatus.class, names = {
            "APPROVED", "REJECTED", "REWORK_REQUIRED"})
    void signContractor_withoutVoting_movesToMatchingStatus(ApprovementProcessItemStatus itemStatus) {
        DocumentVersion version = version(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        ApprovementProcessItem item = approvementItem(1L, null, version, null, itemStatus);

        state.signContractor(item);

        DocumentStatus expected = switch (itemStatus) {
            case APPROVED -> DocumentStatus.APPROVED;
            case REJECTED -> DocumentStatus.REJECTED;
            case REWORK_REQUIRED -> DocumentStatus.REWORK_REQUIRED;
            default -> throw new IllegalStateException("Unexpected status");
        };
        assertThat(version.getStatus()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = ApprovementProcessItemStatus.class, names = {
            "PENDING_CONTRACTOR_SIGN", "PENDING_AUTHOR_SIGN", "ABSTAINED"})
    void signContractor_withInvalidItemStatus_throws(ApprovementProcessItemStatus itemStatus) {
        DocumentVersion version = version(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        ApprovementProcessItem item = approvementItem(1L, null, version, null, itemStatus);

        assertThatThrownBy(() -> state.signContractor(item))
                .isInstanceOf(InvalidDocumentStateException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    void signContractor_withPublishedVoting_keepsStatus() {
        DocumentVersion version = version(DocumentStatus.IN_VOTING);
        ApprovementProcess process = process(ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        ApprovementProcessItem item = approvementItem(
                1L, null, version, process, ApprovementProcessItemStatus.APPROVED);

        state.signContractor(item);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.IN_VOTING);
    }

    @Test
    void signContractor_withCompletedVoting_throws() {
        DocumentVersion version = version(DocumentStatus.IN_VOTING);
        ApprovementProcess process = process(ApprovementProcessStatus.VOTING_COMPLETED);
        ApprovementProcessItem item = approvementItem(
                1L, null, version, process, ApprovementProcessItemStatus.APPROVED);

        assertThatThrownBy(() -> state.signContractor(item))
                .isInstanceOf(InvalidDocumentStateException.class)
                .hasMessageContaining("voting");
    }

    @Test
    void sendForSign_fromDraft_requestsAuthorSignature() {
        DocumentVersion version = version(DocumentStatus.DRAFT);

        state.sendForSign(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.PENDING_AUTHOR_SIGN);
    }

    @Test
    void sendForSign_fromAuthorSigned_requestsContractorSignature() {
        DocumentVersion version = version(DocumentStatus.AUTHOR_SIGNED);

        state.sendForSign(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.PENDING_CONTRACTOR_SIGN);
    }

    @Test
    void sendForSign_fromApproved_throws() {
        DocumentVersion version = version(DocumentStatus.APPROVED);

        assertThatThrownBy(() -> state.sendForSign(version))
                .isInstanceOf(InvalidDocumentStateException.class);
    }

    @Test
    void publishForVoting_fromDraft_movesToVoting() {
        DocumentVersion version = version(DocumentStatus.DRAFT);

        state.publishForVoting(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.IN_VOTING);
    }

    @Test
    void publishForVoting_fromApproved_throws() {
        DocumentVersion version = version(DocumentStatus.APPROVED);

        assertThatThrownBy(() -> state.publishForVoting(version))
                .isInstanceOf(InvalidDocumentStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = DocumentStatus.class, names = {
            "DRAFT", "REWORK_REQUIRED", "APPROVED", "REJECTED"})
    void delete_fromAllowedStatuses_movesToDeleted(DocumentStatus status) {
        DocumentVersion version = version(status);

        state.delete(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.DELETED);
    }

    @Test
    void delete_fromPendingContractorSign_throws() {
        DocumentVersion version = version(DocumentStatus.PENDING_CONTRACTOR_SIGN);

        assertThatThrownBy(() -> state.delete(version))
                .isInstanceOf(InvalidDocumentStateException.class);
    }

    @Test
    void modified_fromDraft_movesToReworkRequired() {
        DocumentVersion version = version(DocumentStatus.DRAFT);

        state.modified(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.REWORK_REQUIRED);
    }

    @Test
    void modified_fromReworkRequired_staysInReworkRequired() {
        DocumentVersion version = version(DocumentStatus.REWORK_REQUIRED);

        state.modified(version);

        assertThat(version.getStatus()).isEqualTo(DocumentStatus.REWORK_REQUIRED);
    }

    @Test
    void modified_fromApproved_throws() {
        DocumentVersion version = version(DocumentStatus.APPROVED);

        assertThatThrownBy(() -> state.modified(version))
                .isInstanceOf(InvalidDocumentStateException.class);
    }

    private DocumentVersion version(DocumentStatus status) {
        DocumentVersion version = new DocumentVersion();
        version.setId(1L);
        version.setStatus(status);
        version.setApprovementProcesses(new ArrayList<>());
        return version;
    }

    private ApprovementProcess process(ApprovementProcessStatus status) {
        return approvementProcess(1L, null, status);
    }
}
