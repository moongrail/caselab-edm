package ru.caselab.edm.backend.document.application.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.exception.DocumentForbiddenAccess;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAccessPolicy;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAccessRequest;
import ru.caselab.edm.backend.shared.security.authorization.ResourceAction;

import java.util.Objects;
import java.util.UUID;

/**
 * Document-specific rule plugged into the security module's authorization
 * boundary.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentResourceAccessPolicy implements ResourceAccessPolicy {

    public static final String RESOURCE_TYPE = "document";

    private final DocumentRepository documentRepository;

    @Override
    public boolean supports(String resourceType) {
        return RESOURCE_TYPE.equals(resourceType);
    }

    @Override
    public void verify(ResourceAccessRequest request) {
        if (request.action() == ResourceAction.DOWNLOAD) {
            verifyContent(request.resourceId(), request.userId());
            return;
        }
        long documentId = parseDocumentId(request.resourceId());
        verifyOwned(requireDocument(documentId), request.userId());
    }

    public Document requireOwned(long documentId, UUID userId) {
        Document document = requireDocument(documentId);
        verifyOwned(document, userId);
        return document;
    }

    public void verifyOwned(Document document, UUID userId) {
        if (document == null
                || document.getOwnerId() == null
                || !Objects.equals(document.getOwnerId(), userId)) {
            Long documentId = document == null ? null : document.getId();
            log.warn("User {} is not allowed to access document {}", userId, documentId);
            throw new DocumentForbiddenAccess(
                    "You don't have access to this document with id = %s".formatted(documentId));
        }
    }

    public void verifyContent(String contentUrl, UUID userId) {
        if (contentUrl == null || contentUrl.isBlank()
                || !documentRepository.existsAccessibleByContentUrl(contentUrl, userId)) {
            throw new DocumentForbiddenAccess("Download access to the requested document is forbidden");
        }
    }

    private Document requireDocument(long documentId) {
        return documentRepository.findWithUserById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    private long parseDocumentId(String resourceId) {
        try {
            return Long.parseLong(resourceId);
        } catch (NumberFormatException ex) {
            throw new DocumentForbiddenAccess("Invalid document identifier");
        }
    }
}
