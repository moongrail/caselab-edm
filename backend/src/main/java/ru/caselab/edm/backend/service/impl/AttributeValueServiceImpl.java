package ru.caselab.edm.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.AttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.AttributeValueDTO;
import ru.caselab.edm.backend.dto.AttributeValueUpdateDTO;
import ru.caselab.edm.backend.entity.AttributeValue;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.AttributeValueMapper;
import ru.caselab.edm.backend.repository.AttributeValueRepository;
import ru.caselab.edm.backend.service.AttributeService;
import ru.caselab.edm.backend.service.AttributeValueService;
import ru.caselab.edm.backend.service.DocumentService;

@Service
public class AttributeValueServiceImpl implements AttributeValueService {


    private final AttributeValueRepository attributeValueRepository;
    private final AttributeService attributeService;
    private final DocumentService documentService;
    private final AttributeValueMapper attributeValueMapper;

    @Autowired
    public AttributeValueServiceImpl(AttributeValueRepository attributeValueRepository, AttributeValueMapper attributeValueMapper, AttributeService attributeService, DocumentService documentService) {
        this.attributeValueRepository = attributeValueRepository;
        this.attributeValueMapper = attributeValueMapper;
        this.documentService = documentService;
        this.attributeService = attributeService;

    }

    @Override
    @Transactional
    public AttributeValueDTO getAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId) {
        AttributeValue attributeValue = attributeValueRepository.findByDocumentIdAndAttributeId(documentId, attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Value doesn't exist"));

        return attributeValueMapper.toDTO(attributeValue);
    }

    @Override
    @Transactional
    public AttributeValueDTO getAttributeValueById(Long id) {
        AttributeValue attributeValue = attributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Value doesn't exist"));

        return attributeValueMapper.toDTO(attributeValue);
    }

    @Transactional
    @Override
    public AttributeValueDTO updateAttributeValue(Long id, AttributeValueUpdateDTO value) {
        AttributeValue updateValue = attributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute value not found: " + id));

        updateValue.setValue(value.getValue());
        updateValue.setAttribute(attributeService.getAttributeMapper().toEntity(attributeService.getAttributeById(value.getAttributeId())));
        updateValue.setDocument(documentService.getDocument(value.getDocumentId()));
        attributeValueRepository.save(updateValue);
        return attributeValueMapper.toDTO(updateValue);
    }

    @Transactional
    @Override
    public AttributeValueDTO createAttributeValue(AttributeValueCreateDTO value) {
        AttributeValue createValue = AttributeValue.builder()
                .document(documentService.getDocument(value.getDocumentId()))
                .attribute(attributeService.getAttributeMapper().toEntity(attributeService.getAttributeById(value.getAttributeId())))
                .value(value.getValue())
                .build();
        return attributeValueMapper.toDTO(attributeValueRepository.save(createValue));
    }

    @Transactional
    @Override
    public void deleteAttributeValue(Long id) {
        attributeValueRepository.deleteById(id);
    }
}
