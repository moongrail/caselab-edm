package ru.caselab.edm.backend.document.application.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.exception.DocumentForbiddenAccess;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAccessRequest;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAction;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentResourceAccessPolicyTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private DocumentRepository documentRepository;

    private DocumentResourceAccessPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DocumentResourceAccessPolicy(documentRepository);
    }

    @Test
    void supportsOnlyDocumentResourceType() {
        assertThat(policy.supports(DocumentResourceAccessPolicy.RESOURCE_TYPE)).isTrue();
        assertThat(policy.supports("department")).isFalse();
    }

    @Test
    void verifiesContentAccessForDownloadAction() {
        when(documentRepository.existsAccessibleByContentUrl("content/1", OWNER_ID)).thenReturn(true);

        assertThatCode(() -> policy.verify(new ResourceAccessRequest(
                DocumentResourceAccessPolicy.RESOURCE_TYPE,
                "content/1",
                OWNER_ID,
                ResourceAction.DOWNLOAD))).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnknownOrBlankContent() {
        when(documentRepository.existsAccessibleByContentUrl("unknown", OWNER_ID)).thenReturn(false);

        assertThatThrownBy(() -> policy.verify(new ResourceAccessRequest(
                DocumentResourceAccessPolicy.RESOURCE_TYPE,
                "unknown",
                OWNER_ID,
                ResourceAction.DOWNLOAD)))
                .isInstanceOf(DocumentForbiddenAccess.class);
        assertThatThrownBy(() -> policy.verify(new ResourceAccessRequest(
                DocumentResourceAccessPolicy.RESOURCE_TYPE,
                " ",
                OWNER_ID,
                ResourceAction.DOWNLOAD)))
                .isInstanceOf(DocumentForbiddenAccess.class);
    }

    @Test
    void verifiesOwnershipByDocumentIdentifierForReadAction() {
        when(documentRepository.findWithUserById(7L))
                .thenReturn(Optional.of(document(OWNER_ID)));

        assertThatCode(() -> policy.verify(new ResourceAccessRequest(
                DocumentResourceAccessPolicy.RESOURCE_TYPE,
                "7",
                OWNER_ID,
                ResourceAction.READ))).doesNotThrowAnyException();
    }

    @Test
    void rejectsNonNumericDocumentIdentifier() {
        assertThatThrownBy(() -> policy.verify(new ResourceAccessRequest(
                DocumentResourceAccessPolicy.RESOURCE_TYPE,
                "content/1",
                OWNER_ID,
                ResourceAction.READ)))
                .isInstanceOf(DocumentForbiddenAccess.class)
                .hasMessageContaining("identifier");
        verifyNoInteractions(documentRepository);
    }

    private Document document(UUID ownerId) {
        Document document = new Document();
        document.setId(7L);
        document.setOwnerId(ownerId);
        return document;
    }
}
