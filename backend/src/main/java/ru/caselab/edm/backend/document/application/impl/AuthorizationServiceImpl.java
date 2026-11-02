package ru.caselab.edm.backend.document.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.document.application.security.DocumentResourceAccessPolicy;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.exception.DocumentForbiddenAccess;
import ru.caselab.edm.backend.document.application.AuthorizationService;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAccessRequest;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAction;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAuthorizationService;

import java.util.UUID;

/**
 * Facade kept for document services. The decision is delegated to the
 * security module, while document-specific knowledge lives in
 * {@link DocumentResourceAccessPolicy}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final DocumentResourceAccessPolicy documentPolicy;
    private final ResourceAuthorizationService resourceAuthorizationService;

    @Override
    public void checkDocumentOwner(Document document, UUID userId) {
        documentPolicy.verifyOwned(document, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Document requireOwnedDocument(long documentId, UUID userId) {
        return documentPolicy.requireOwned(documentId, userId);
    }

    @Override
    public void ensureContentAccess(String contentUrl, UUID userId) {
        if (contentUrl == null || contentUrl.isBlank()) {
            throw new DocumentForbiddenAccess("Download access to the requested document is forbidden");
        }
        resourceAuthorizationService.verify(new ResourceAccessRequest(
                DocumentResourceAccessPolicy.RESOURCE_TYPE,
                contentUrl,
                userId,
                ResourceAction.DOWNLOAD
        ));
    }
}
