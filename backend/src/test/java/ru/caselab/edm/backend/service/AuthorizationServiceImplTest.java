package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.service.impl.AuthorizationServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private AuthorizationServiceImpl authorizationService;

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
    void ensureContentAccess_allowsAccessibleContent() {
        when(documentRepository.existsAccessibleByContentUrl("content/1", OWNER_ID)).thenReturn(true);

        assertThatCode(() -> authorizationService.ensureContentAccess("content/1", OWNER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureContentAccess_rejectsForeignContent() {
        when(documentRepository.existsAccessibleByContentUrl("content/1", OWNER_ID)).thenReturn(false);

        assertThatThrownBy(() -> authorizationService.ensureContentAccess("content/1", OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class)
                .hasMessageContaining("Download access");
    }

    @Test
    void ensureContentAccess_rejectsBlankContentWithoutQuery() {
        assertThatThrownBy(() -> authorizationService.ensureContentAccess(" ", OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);
        assertThatThrownBy(() -> authorizationService.ensureContentAccess(null, OWNER_ID))
                .isInstanceOf(DocumentForbiddenAccess.class);

        verify(documentRepository, never()).existsAccessibleByContentUrl(" ", OWNER_ID);
    }

    private Document document(UUID ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        Document document = new Document();
        document.setId(DOCUMENT_ID);
        document.setUser(owner);
        return document;
    }
}
