package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.exceptions.DocumentTypeAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.DocumentTypeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.DocumentTypeService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeImpl implements DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;
    private final AttributeRepository attributeRepository;
    private final DocumentTypeMapper mapper;

    public Page<DocumentTypeDTO> getAllDocumentType(int page, int size) {
        log.info("Get all document type - page: {}, size: {}", page, size);
        Page<DocumentType> documentTypes = documentTypeRepository.findAll(
                PageRequest.of(page, size)
        );
        log.info("Get {} document type", documentTypes.getTotalElements());

        return documentTypes.map(mapper::toDto);
    }

    public DocumentTypeDTO getDocumentTypeById(Long id) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Document type with id: {} not found", id);
                    return new ResourceNotFoundException("Document type with id: %s not found ".formatted(id));
                });
        log.info("Document type with id: {} found", documentType.getId());
        return mapper.toDto(documentType);
    }

    public DocumentTypeDTO createDocumentType(DocumentTypeCreateDTO createdDocumentType) {
        log.info("Creating document type with name: {}", createdDocumentType.getName());
        if (!documentTypeRepository.findByName(createdDocumentType.getName()).isEmpty()) {
            throw new DocumentTypeAlreadyExistsException("Document type with name %s already exists"
                    .formatted(createdDocumentType.getName()));
        }

        log.debug("Document type data: {}", createdDocumentType);
        DocumentType documentType = new DocumentType();
        documentType.setName(createdDocumentType.getName());
        documentType.setDescription(createdDocumentType.getDescription());
        if (createdDocumentType.getAttributeIds() != null
                && !createdDocumentType.getAttributeIds().isEmpty()) {
            documentType.setAttributes(mapAttributeIdsToEntities(createdDocumentType.getAttributeIds()));
        }

        documentTypeRepository.save(documentType);
        log.info("Document type created with id: {}", documentType.getId());

        return mapper.toDto(documentType);
    }

    public DocumentTypeDTO updateDocumentType(Long id, DocumentTypeUpdateDTO updateDocumentType) {
        log.info("Updating document type with id: {}", id);
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Document type with id: {} not found", id);
                    return new ResourceNotFoundException("Not Found: " + id);
                });
        if (!documentTypeRepository.findByName(updateDocumentType.getName()).isEmpty()) {
            throw new DocumentTypeAlreadyExistsException("Document type with name %s already exists"
                    .formatted(updateDocumentType.getName()));
        }
        documentType.setName(updateDocumentType.getName());
        documentType.setDescription(updateDocumentType.getDescription());
        documentType.setAttributes(mapAttributeIdsToEntities(updateDocumentType.getAttributeIds()));

        documentTypeRepository.save(documentType);
        log.info("Document type update successfully: {}", documentType);
        return mapper.toDto(documentType);
    }

    public void deleteDocumentType(Long id) {
        log.info("Deleting document type with id: {}", id);
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Document type with id: {} not found", id);
                    return new ResourceNotFoundException("Not Found: " + id);
                });
        documentTypeRepository.deleteById(id);
        log.info("Document type with id: {} deleted successfully", id);
    }

    private Set<Attribute> mapAttributeIdsToEntities(Set<Long> attributeIds) {
        return new HashSet<>(attributeRepository.findAllById(attributeIds));
    }

    @Override
    public DocumentTypeMapper getDocumentTypeMapper() {
        return mapper;
    }
}
