package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.MinioSaveDto;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.DocumentAttributeValueMapper;
import ru.caselab.edm.backend.mapper.MinioDocumentMapper;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.DocumentTypeService;
import ru.caselab.edm.backend.service.DocumentVersionService;
import ru.caselab.edm.backend.service.MinioService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final MinioService minioService;
    private final DocumentTypeService documentTypeService;
    private final DocumentAttributeValueService documentAttributeValueService;
    private final MinioDocumentMapper minioDocumentMapper;
    private final DocumentAttributeValueMapper documentAttributeValueMapper;


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

        List<DocumentAttributeValue> documentAttributeValueList =
                documentAttributeValueService.createDocumentAttributeValues(document.getAttributeValues(), newDocument, newDocumentVersion);

        newDocumentVersion.setDocumentAttributeValue(documentAttributeValueList);

        documentVersionRepository.save(newDocumentVersion);

        return newDocumentVersion;
    }
}
