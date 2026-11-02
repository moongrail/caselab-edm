package ru.caselab.edm.backend.document.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.document.application.security.DocumentResourceAccessPolicy;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.exception.DocumentForbiddenAccess;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.backend.document.application.impl.AuthorizationServiceImpl;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAccessRequest;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAction;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAuthorizationService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    private static final long DOCUMENT_ID = 10L;
    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ResourceAuthorizationService resourceAuthorizationService;

    private DocumentResourceAccessPolicy documentPolicy;
    private AuthorizationServiceImpl authorizationService;

    @BeforeEach
    void setUp() {
        documentPolicy = new DocumentResourceAccessPolicy(documentRepository);
        authorizationService = new AuthorizationServiceImpl(documentPolicy, resourceAuthorizationService);
    }

    @Test
    void checkDocumentOwner_allowsOwner() {
        Document document = document(OWNER_ID);

        assertThatCode(() -> authorizationService.checkDocumentOwner(document, OWNER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void checkDocumentOwner_rejectsForeignUser() {
        Document document = document(OWNER_ID);

        assertThatThrownBy(() -> authorizationService.checkDocumentOwner(document, OTHER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class)
                .hasMessageContaining(String.valueOf(DOCUMENT_ID));
    }

    @Test
    void checkDocumentOwner_rejectsDocumentWithoutOwner() {
        Document document = document(null);

        assertThatThrownBy(() -> authorizationService.checkDocumentOwner(document, OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);
    }

    @Test
    void checkDocumentOwner_rejectsNullDocument() {
        assertThatThrownBy(() -> authorizationService.checkDocumentOwner(null, OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);
    }

    @Test
    void requireOwnedDocument_returnsOwnedDocument() {
        Document document = document(OWNER_ID);
        when(documentRepository.findWithUserById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        assertThat(authorizationService.requireOwnedDocument(DOCUMENT_ID, OWNER_ID))
                .isSameAs(document);
    }

    @Test
    void requireOwnedDocument_missingDocument_throwsNotFound() {
        when(documentRepository.findWithUserById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizationService.requireOwnedDocument(DOCUMENT_ID, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void requireOwnedDocument_foreignDocument_throwsForbidden() {
        when(documentRepository.findWithUserById(DOCUMENT_ID))
                .thenReturn(Optional.of(document(OWNER_ID)));

        assertThatThrownBy(() -> authorizationService.requireOwnedDocument(DOCUMENT_ID, OTHER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);
    }

    @Test
    void ensureContentAccess_delegatesToSecurityModule() {
        assertThatCode(() -> authorizationService.ensureContentAccess("content/1", OWNER_ID))
                .doesNotThrowAnyException();

        ArgumentCaptor<ResourceAccessRequest> captor = ArgumentCaptor.forClass(ResourceAccessRequest.class);
        verify(resourceAuthorizationService).verify(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new ResourceAccessRequest(
                        DocumentResourceAccessPolicy.RESOURCE_TYPE,
                        "content/1",
                        OWNER_ID,
                        ResourceAction.DOWNLOAD));
    }

    @Test
    void ensureContentAccess_rejectsBlankContentWithoutQuery() {
        assertThatThrownBy(() -> authorizationService.ensureContentAccess(" ", OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);
        assertThatThrownBy(() -> authorizationService.ensureContentAccess(null, OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);

        verify(resourceAuthorizationService, never()).verify(any());
        verify(documentRepository, never()).existsAccessibleByContentUrl(any(), any());
    }

    private Document document(UUID ownerId) {
        Document document = new Document();
        document.setId(DOCUMENT_ID);
        document.setOwnerId(ownerId);
        return document;
    }
}
