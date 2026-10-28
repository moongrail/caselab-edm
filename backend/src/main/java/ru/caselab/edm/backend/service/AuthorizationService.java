package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.entity.Document;

import java.util.UUID;

/**
 * Resource-level authorization for documents.
 *
 * <p>Role checks ({@code @PreAuthorize}) stay at the API boundary. This service
 * centralizes ownership, signatory and content-access rules so they cannot
 * diverge between controllers and domain services.</p>
 */
public interface AuthorizationService {

    /**
     * Ensures that the user owns the document.
     *
     * @throws ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess if access is denied
     */
    void checkDocumentOwner(Document document, UUID userId);

    /**
     * Loads a document and ensures that the user owns it.
     *
     * @throws ru.caselab.edm.backend.exceptions.ResourceNotFoundException if the document does not exist
     * @throws ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess if the user is not the owner
     */
    Document requireOwnedDocument(long documentId, UUID userId);

    /**
     * Ensures that the user may download the object identified by {@code contentUrl}.
     *
     * @throws ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess if access is denied
     */
    void ensureContentAccess(String contentUrl, UUID userId);
}
