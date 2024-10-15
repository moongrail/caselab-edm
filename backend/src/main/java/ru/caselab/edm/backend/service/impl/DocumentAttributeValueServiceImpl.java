package ru.caselab.edm.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.DocumentAttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.DocumentAttributeValueMapper;
import ru.caselab.edm.backend.repository.DocumentAttributeValueRepository;
import ru.caselab.edm.backend.service.AttributeService;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;
import ru.caselab.edm.backend.service.DocumentVersionService;

@Service
public class DocumentAttributeValueServiceImpl implements DocumentAttributeValueService {


    private final DocumentAttributeValueRepository documentAttributeValueRepository;
    private final AttributeService attributeService;
    private final DocumentAttributeValueMapper documentAttributeValueMapper;
    private final DocumentVersionService documentVersionService;

    @Autowired
    public DocumentAttributeValueServiceImpl(DocumentAttributeValueRepository documentAttributeValueRepository,
                                             DocumentAttributeValueMapper documentAttributeValueMapper, AttributeService attributeService,
                                             DocumentVersionService documentVersionService) {
        this.documentAttributeValueRepository = documentAttributeValueRepository;
        this.documentAttributeValueMapper = documentAttributeValueMapper;
        this.attributeService = attributeService;
        this.documentVersionService = documentVersionService;

    }

    @Override
    @Transactional(readOnly = true)
    public DocumentAttributeValueDTO getDocumentAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId) {
        DocumentAttributeValue documentAttributeValue = documentAttributeValueRepository.findByDocumentVersionIdAndAttributeId(documentId, attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Value doesn't exist"));

        return documentAttributeValueMapper.toDTO(documentAttributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentAttributeValueDTO getDocumentAttributeValueById(Long id) {
        DocumentAttributeValue documentAttributeValue = documentAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Value doesn't exist"));

        return documentAttributeValueMapper.toDTO(documentAttributeValue);
    }

    @Transactional
    @Override
    public DocumentAttributeValueDTO updateDocumentAttributeValue(Long id, DocumentAttributeValueUpdateDTO value) {
        DocumentAttributeValue updateValue = documentAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute value not found: " + id));

        updateValue.setValue(value.getValue());
        updateValue.setAttribute(attributeService.getAttributeMapper().toEntity(attributeService.getAttributeById(value.getAttributeId())));
        updateValue.setDocumentVersion(documentVersionService.getDocumentVersion(value.getDocumentId()));
        documentAttributeValueRepository.save(updateValue);
        return documentAttributeValueMapper.toDTO(updateValue);
    }

    @Transactional
    @Override
    public DocumentAttributeValueDTO createDocumentAttributeValue(DocumentAttributeValueCreateDTO value) {
        DocumentAttributeValue createValue = DocumentAttributeValue.builder()
                .documentVersion(documentVersionService.getDocumentVersion(value.getDocumentId()))
                .attribute(attributeService.getAttributeMapper().toEntity(attributeService.getAttributeById(value.getAttributeId())))
                .value(value.getValue())
                .build();
        return documentAttributeValueMapper.toDTO(documentAttributeValueRepository.save(createValue));
    }

    @Transactional
    @Override
    public void deleteAttributeValue(Long id) {
        documentAttributeValueRepository.deleteById(id);
    }
}
