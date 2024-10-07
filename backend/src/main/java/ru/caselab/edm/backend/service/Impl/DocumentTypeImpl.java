package ru.caselab.edm.backend.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.DocumentTypeMapper;
import ru.caselab.edm.backend.model.DocumentType;
import ru.caselab.edm.backend.repository.AttributesRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.DocumentTypeService;

import java.util.Collections;

@Service
public class DocumentTypeImpl implements DocumentTypeService {
    @Autowired
    private final DocumentTypeRepository documentTypeRepository;
    @Autowired
    private final AttributesRepository attributesRepository;
    @Autowired
    private final DocumentTypeMapper mapper;

    public DocumentTypeImpl(DocumentTypeRepository documentTypeRepository, AttributesRepository attributesRepository, DocumentTypeMapper mapper) {
        this.documentTypeRepository = documentTypeRepository;
        this.attributesRepository = attributesRepository;
        this.mapper = mapper;
    }

    public Page<DocumentTypeDTO> getAllDocumentType(int page, int size) {
        Page<DocumentType> documentTypes = documentTypeRepository.findAll(
                PageRequest.of(page, size)
        );
        return documentTypes.map(mapper::map);
    }

    public DocumentTypeDTO getDocumentTypeById(Long id) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        return mapper.map(documentType);
    }

    public DocumentTypeDTO createDocumentType(DocumentTypeCreateDTO createdDocumentType) {
        DocumentType documentType = new DocumentType();

        documentType.setName(createdDocumentType.getName());
        documentType.setDescription(createdDocumentType.getDescription());
        documentType.setAttributes(attributesRepository.findAllById(createdDocumentType.getAttributesDocumentTypeId()));

        documentTypeRepository.save(documentType);

        return mapper.map(documentType);
    }

    public DocumentTypeDTO updateDocumentType(Long id, DocumentTypeUpdateDTO updateDocumentType) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        documentType.setName(updateDocumentType.getName());
        documentType.setDescription(updateDocumentType.getDescription());
        documentType.setAttributes(attributesRepository.findAllById(Collections.singleton(updateDocumentType.getDocumentTypeId())));

        documentTypeRepository.save(documentType);

        return mapper.map(documentType);
    }

    public void deleteDocumentType(Long id) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        documentTypeRepository.deleteById(id);
    }
}
