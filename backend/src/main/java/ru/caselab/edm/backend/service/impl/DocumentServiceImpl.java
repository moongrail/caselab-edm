package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DocumentService;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Override
    public Page<Document> getAllDocuments(int page, int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Document getDocument(long id) {
        return documentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    @Override
    public Page<Document> getAllDocumentForUser(int page, int size, UUID userId) {
        int offset = page * size;
        return documentRepository.getAllDocumentForUser(userId, size, offset);
    }

    @Override
    public Document getDocumentForUser(long id, UUID userId) {
        return documentRepository.getDocumentForUser(id, userId);
    }

    @Transactional
    @Override
    public Document saveDocument(DocumentCreateDTO document) {
        Document newDocument = new Document();

        newDocument.setDocumentType(
                documentTypeRepository.findById(document.getDocumentTypeId())
                        .orElseThrow(() -> new NoSuchElementException("Document type not found"))
        );

        newDocument.setUser(
                userRepository.findById(document.getUserId())
                        .orElseThrow(() -> new NoSuchElementException("User not found"))
        );

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentName(document.getName());
        documentVersion.setCreatedAt(Instant.now());
        //TODO: cюда ссылку когда minio подключат
        documentVersion.setContentUrl("ContentUrl");

        documentVersionRepository.save(documentVersion);


        return documentRepository.save(newDocument);
    }

    @Transactional
    @Override
    public Document updateDocument(long id, DocumentUpdateDTO document) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        if (document.getDocumentTypeId() != null &&
                document.getDocumentTypeId().equals(existingDocument.getDocumentType().getId())) {
            existingDocument.setDocumentType(
                    documentTypeRepository.findById(document.getDocumentTypeId())
                            .orElseThrow(() -> new NoSuchElementException("Document type not found"))
            );
        }

        if (document.getUserId() != null) {
            existingDocument.setUser(
                    userRepository.findById(document.getUserId())
                            .orElseThrow(() -> new NoSuchElementException("User not found"))
            );
        }

        DocumentVersion documentVersion = new DocumentVersion();

        if (existingDocument.getDocumentVersion() != null)
            documentVersion = getUpdatedDocumentVersion(document, existingDocument);
//TODO: ЛОГИКУ НАПИСАТЬ ПО ДОКУМЕНТ ВЕРСИЯМ
//        documentVersion.setUpdatedAt(Instant.now());

        documentVersionRepository.save(documentVersion);

        return documentRepository.save(existingDocument);
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
}
