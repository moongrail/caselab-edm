package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.entity.Signature;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.SignatureRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DocumentService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureRepository signatureRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<Document> getAllDocuments(int page, int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<DocumentVersion> getAllDocumentVersions(int page, int size) {
        return documentVersionRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Document getDocument(long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }


    @Override
    public DocumentVersion getDocumentVersion(long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        System.out.println(document.getDocumentVersion().get(document.getDocumentVersion().size() - 1).getId());

        return document.getDocumentVersion().get(document.getDocumentVersion().size() - 1);
    }

    @Override
    public Page<Document> getAllDocumentForUser(int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.getAllDocumentForUser(userId, pageable);
    }

    @Override
    public Document getDocumentForUser(long id, UUID userId) {
        getDocument(id);
        return documentRepository.getDocumentForUser(id, userId)
                .orElseThrow(() -> new DocumentForbiddenAccess("Access to the document is forbidden"));
    }

    @Transactional
    @Override
    public DocumentVersion saveDocument(DocumentCreateDTO document) {
        Document newDocument = new Document();

        newDocument.setDocumentType(
                documentTypeRepository.findById(document.getDocumentTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Document type not found"))
        );

        newDocument.setUser(
                userRepository.findById(document.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
        );

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentName(document.getName());
        documentVersion.setCreatedAt(Instant.now());
        documentVersion.setUpdatedAt(Instant.now());
        //TODO: cюда ссылку когда minio подключат
        documentVersion.setContentUrl("ContentUrl");

        newDocument = documentRepository.save(newDocument);

        documentVersion.setDocument(newDocument);
        documentVersion = documentVersionRepository.save(documentVersion);

        System.out.println(documentVersion);

        return documentVersion;
    }

    @Transactional
    @Override
    public DocumentVersion updateDocument(long id, DocumentUpdateDTO document) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (document.getDocumentTypeId() != null &&
                document.getDocumentTypeId().equals(existingDocument.getDocumentType().getId())) {
            existingDocument.setDocumentType(
                    documentTypeRepository.findById(document.getDocumentTypeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Document type not found"))
            );
        }

        if (document.getUserId() != null) {
            existingDocument.setUser(
                    userRepository.findById(document.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"))
            );
        }

        DocumentVersion documentVersion = new DocumentVersion();

        if (existingDocument.getDocumentVersion() != null)
            documentVersion = getUpdatedDocumentVersion(document, existingDocument);

        existingDocument = documentRepository.save(existingDocument);

        documentVersion.setDocument(existingDocument);

        return documentVersionRepository.save(documentVersion);
    }

    private static DocumentVersion getUpdatedDocumentVersion(DocumentUpdateDTO document, Document existingDocument) {

        // изменения только в последнюю версию документа
        DocumentVersion documentVersion = existingDocument.getDocumentVersion()
                .get(existingDocument.getDocumentVersion().size() - 1);
        if (document.getName() != null) {
            documentVersion.setDocumentName(document.getName());
        }

        // TODO: спросить может ли быть contentUrl пустым
        if (document.getContentUrl() != null && !document.getContentUrl().isEmpty()) {
            documentVersion.setContentUrl(document.getContentUrl());
        }

        documentVersion.setUpdatedAt(Instant.now());

        return documentVersion;
    }

    private void validateDate(DocumentVersion documentVersion) {
        if (documentVersion.getCreatedAt().isAfter(documentVersion.getUpdatedAt())) {
            throw new WrongDateException("The creation date cannot be later than the update date");
        }
    }

    @Transactional
    @Override
    public void deleteDocument(long id) {
        documentRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void sendForSign(List<UUID> userIds, Long documentVersionId) {
        Optional<DocumentVersion> documentVersionOptional = documentVersionRepository.findById(documentVersionId);
        if (documentVersionOptional.isEmpty()) {
            throw new ResourceNotFoundException("Document version not found with id = %d".formatted(documentVersionId));
        }
        DocumentVersion documentVersion = documentVersionOptional.get();
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            Signature signature = new Signature();
            signature.setUser(user);
            signature.setDocumentVersion(documentVersion);
            signatureRepository.save(signature);
            eventPublisher.publishEvent(new DocumentSignRequestEvent(this, signature));
        }

    }
}
