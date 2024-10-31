package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.MinioSaveDto;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.MinioDocumentMapper;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;
import ru.caselab.edm.backend.service.DocumentVersionService;
import ru.caselab.edm.backend.service.MinioService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final MinioService minioService;
    private final DocumentAttributeValueService documentAttributeValueService;
    private final MinioDocumentMapper minioDocumentMapper;

    @Override
    public DocumentVersion getDocumentVersion(long id) {

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        return document.getDocumentVersion().get(document.getDocumentVersion().size() - 1);
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
    public DocumentVersion updateDocumentVersion(DocumentUpdateDTO document, Document existingDocument, UUID userId) {
        DocumentVersion updatingDocumentVersion = new DocumentVersion();
        DocumentVersion exsistingVersion = existingDocument.getDocumentVersion()
                .stream()
                .max(Comparator.comparing(DocumentVersion::getCreatedAt))
                .orElseThrow();
        updatingDocumentVersion.setDocument(existingDocument);

        updatingDocumentVersion = getUpdatedDocumentVersion(document, updatingDocumentVersion, exsistingVersion, userId);

        return documentVersionRepository.save(updatingDocumentVersion);
    }

    private DocumentVersion getUpdatedDocumentVersion(DocumentUpdateDTO document,
                                                      DocumentVersion updatingDocumentVersion,
                                                      DocumentVersion exsistingVersion,
                                                      UUID userId) {


        if (document.getDocumentName() != null) {
            updatingDocumentVersion.setDocumentName(document.getDocumentName());

        } else {
            updatingDocumentVersion.setDocumentName(exsistingVersion.getDocumentName());
        }
        String documentName = updatingDocumentVersion.getDocumentName();

        // TODO: спросить может ли быть contentUrl пустым
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

        return updatingDocumentVersion;
    }
}
