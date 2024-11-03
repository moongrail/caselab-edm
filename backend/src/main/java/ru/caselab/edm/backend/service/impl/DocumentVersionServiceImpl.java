package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.minio.MinioSaveDto;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.document.MinioDocumentMapper;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;
import ru.caselab.edm.backend.service.DocumentVersionService;
import ru.caselab.edm.backend.service.MinioService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final MinioService minioService;
    private final DocumentAttributeValueService documentAttributeValueService;
    private final MinioDocumentMapper minioDocumentMapper;

    @Override
    public DocumentVersion getDocumentVersion(long documentId) {
        List<DocumentVersion> documentVersion = documentVersionRepository.findByDocumentId(documentId);

        DocumentVersion latestDocumentVersion = documentVersion
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow();

        return latestDocumentVersion;
    }

    @Override
    public List<DocumentVersion> getAllDocumentVersions(Long documentId) {
        return documentVersionRepository.findByDocumentId(documentId);
    }

    @Override
    public Page<DocumentVersion> getAllDocumentVersions(int page, int size) {
        return documentVersionRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public DocumentVersion saveDocumentVersion(DocumentCreateDTO document, Document newDocument, UUID userId) {

        DocumentVersion newDocumentVersion = new DocumentVersion();
        newDocumentVersion.setDocumentName(document.getDocumentName());
        newDocumentVersion.setDocument(newDocument);

        MinioSaveDto saveDto = minioDocumentMapper.map(document, userId);
        minioService.saveObject(saveDto);
        newDocumentVersion.setContentUrl(saveDto.objectName());

        documentVersionRepository.save(newDocumentVersion);

        List<DocumentAttributeValue> documentAttributeValueList =
                documentAttributeValueService.createDocumentAttributeValues(document.getAttributeValues(), newDocument, newDocumentVersion);

        newDocumentVersion.setDocumentAttributeValue(documentAttributeValueList);

        return newDocumentVersion;
    }

    @Transactional
    @Override
    public DocumentVersion updateDocumentVersion(DocumentUpdateDTO document, Document existingDocument, UUID userId) {
        DocumentVersion updatingDocumentVersion = new DocumentVersion();
        DocumentVersion exsistingVersion = existingDocument.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow();
        updatingDocumentVersion.setDocument(existingDocument);

        if (document.getDocumentName() != null) {
            updatingDocumentVersion.setDocumentName(document.getDocumentName());
        } else {
            updatingDocumentVersion.setDocumentName(exsistingVersion.getDocumentName());
        }
        String documentName = updatingDocumentVersion.getDocumentName();

        if (document.getData() != null && !document.getData().isEmpty()) {
            String data = document.getData();
            MinioSaveDto saveDto = minioDocumentMapper.map(documentName, data, userId);
            minioService.saveObject(saveDto);
            updatingDocumentVersion.setContentUrl(saveDto.objectName());
        } else {
            updatingDocumentVersion.setContentUrl(exsistingVersion.getContentUrl());
        }

        documentVersionRepository.saveAndFlush(updatingDocumentVersion);

        List<DocumentAttributeValue> documentAttributeValueList =
                documentAttributeValueService.updateDocumentAttributeValue(document.getAttributeValues(),
                        exsistingVersion,
                        updatingDocumentVersion
                );

        updatingDocumentVersion.setDocumentAttributeValue(documentAttributeValueList);

        return documentVersionRepository.save(updatingDocumentVersion);
    }
}
