package ru.caselab.edm.backend.document.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.document.api.mapper.MinioDocumentMapper;
import ru.caselab.edm.backend.document.application.DocumentAttributeValueService;
import ru.caselab.edm.backend.document.application.DocumentVersionService;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentAttributeValue;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;
import ru.caselab.edm.backend.document.infrastructure.DocumentVersionRepository;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.platform.storage.StorageObject;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final ObjectStorage objectStorage;
    private final DocumentAttributeValueService documentAttributeValueService;
    private final MinioDocumentMapper minioDocumentMapper;

    @Override
    public DocumentVersion getDocumentVersion(long documentId) {
        List<DocumentVersion> documentVersion = documentVersionRepository.findByDocumentId(documentId);

        return documentVersion
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt)
                        .thenComparing(DocumentVersion::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document version not found for document id = %d".formatted(documentId)));
    }

    @Override
    public List<DocumentVersion> getAllDocumentVersions(Long documentId) {
        return documentVersionRepository.findByDocumentId(documentId);
    }

    @Override
    public Page<DocumentVersion> getAllDocumentVersions(int page, int size) {
        return documentVersionRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    @Override
    public DocumentVersion saveDocumentVersion(DocumentCreateDTO document, Document newDocument,
                                               UUID userId) {
        DocumentVersion newDocumentVersion = new DocumentVersion();
        newDocumentVersion.setDocumentName(document.getDocumentName());
        newDocumentVersion.setDocument(newDocument);
        newDocumentVersion.setStatus(DocumentStatus.DRAFT);

        StorageObject storageObject = minioDocumentMapper.map(document, userId);
        objectStorage.put(storageObject);
        newDocumentVersion.setContentUrl(storageObject.objectName());

        documentVersionRepository.save(newDocumentVersion);

        List<DocumentAttributeValue> documentAttributeValueList =
                documentAttributeValueService.createDocumentAttributeValues(
                        document.getAttributeValues(), newDocument, newDocumentVersion);

        newDocumentVersion.setDocumentAttributeValue(documentAttributeValueList);

        return newDocumentVersion;
    }

    @Transactional
    @Override
    public DocumentVersion updateDocumentVersion(DocumentUpdateDTO document,
                                                 Document existingDocument,
                                                 UUID userId) {
        DocumentVersion updatingDocumentVersion = new DocumentVersion();
        DocumentVersion existingVersion = existingDocument.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt)
                        .thenComparing(DocumentVersion::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document version not found for document id = %d"
                                .formatted(existingDocument.getId())));
        updatingDocumentVersion.setDocument(existingDocument);

        existingVersion.getStateMachine().modified(existingVersion);

        if (document.getDocumentName() != null) {
            updatingDocumentVersion.setDocumentName(document.getDocumentName());
        } else {
            updatingDocumentVersion.setDocumentName(existingVersion.getDocumentName());
        }

        if (document.getFile() != null) {
            StorageObject storageObject = minioDocumentMapper.map(document.getFile().fileName(),
                    document.getFile().data(), userId);
            objectStorage.put(storageObject);
            updatingDocumentVersion.setContentUrl(storageObject.objectName());
        } else {
            updatingDocumentVersion.setContentUrl(existingVersion.getContentUrl());
        }

        updatingDocumentVersion.setStatus(DocumentStatus.DRAFT);
        documentVersionRepository.saveAndFlush(updatingDocumentVersion);

        List<DocumentAttributeValue> documentAttributeValueList =
                documentAttributeValueService.updateDocumentAttributeValue(
                        document.getAttributeValues(), existingVersion, updatingDocumentVersion);

        updatingDocumentVersion.setDocumentAttributeValue(documentAttributeValueList);

        return documentVersionRepository.save(updatingDocumentVersion);
    }
}
