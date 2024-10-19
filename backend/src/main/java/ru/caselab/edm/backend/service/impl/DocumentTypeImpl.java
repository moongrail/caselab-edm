package ru.caselab.edm.backend.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.DocumentTypeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.DocumentTypeService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentTypeImpl implements DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;
    private final AttributeRepository attributeRepository;
    private final DocumentTypeMapper mapper;

    public Page<DocumentTypeDTO> getAllDocumentType(int page, int size) {
        Page<DocumentType> documentTypes = documentTypeRepository.findAll(
                PageRequest.of(page, size)
        );
        return documentTypes.map(mapper::toDto);
    }

    public DocumentTypeDTO getDocumentTypeById(Long id) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        return mapper.toDto(documentType);
    }

    public DocumentTypeDTO createDocumentType(DocumentTypeCreateDTO createdDocumentType) {
        DocumentType documentType = new DocumentType();

        documentType.setName(createdDocumentType.getName());
        documentType.setDescription(createdDocumentType.getDescription());
        documentType.setAttributes(mapAttributeIdsToEntities(createdDocumentType.getAttributeIds()));



        documentTypeRepository.save(documentType);

        return mapper.toDto(documentType);
    }

    public DocumentTypeDTO updateDocumentType(Long id, DocumentTypeUpdateDTO updateDocumentType) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        documentType.setName(updateDocumentType.getName());
        documentType.setDescription(updateDocumentType.getDescription());
        documentType.setAttributes(mapAttributeIdsToEntities(updateDocumentType.getAttributeIds()));

        documentTypeRepository.save(documentType);

        return mapper.toDto(documentType);
    }

    public void deleteDocumentType(Long id) {
        DocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        documentTypeRepository.deleteById(id);
    }

    private Set<Attribute> mapAttributeIdsToEntities(Set<Long> attributeIds) {
        return new HashSet<>(attributeRepository.findAllById(attributeIds));
    }

    @Override
    public DocumentTypeMapper getDocumentTypeMapper() {
        return mapper;
    }
}
