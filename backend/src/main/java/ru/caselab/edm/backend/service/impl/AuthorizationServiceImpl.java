package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.service.AuthorizationService;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final DocumentRepository documentRepository;

    @Override
    public void checkDocumentOwner(Document document, UUID userId) {
        if (document == null
                || document.getUser() == null
                || !Objects.equals(document.getUser().getId(), userId)) {
            Long documentId = document == null ? null : document.getId();
            log.warn("User {} is not allowed to access document {}", userId, documentId);
            throw new DocumentForbiddenAccess(
                    "You don't have access to this document with id = %s".formatted(documentId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Document requireOwnedDocument(long documentId, UUID userId) {
        Document document = documentRepository.findWithUserById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        checkDocumentOwner(document, userId);
        return document;
    }

    @Override
    public void ensureContentAccess(String contentUrl, UUID userId) {
        if (contentUrl == null || contentUrl.isBlank()
                || !documentRepository.existsAccessibleByContentUrl(contentUrl, userId)) {
            throw new DocumentForbiddenAccess("Download access to the requested document is forbidden");
        }
    }
}
