package ru.caselab.edm.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                .orElseThrow(() -> {
                    log.warn("Document attribute value not found with document id: {} and attribute id: {}", documentId,attributeId);
                    return new ResourceNotFoundException("Value doesn't exist");
                });
        log.info("Document Attribute value with document id: {} and attribute id: {} found", documentId,attributeId);
        return documentAttributeValueMapper.toDTO(documentAttributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentAttributeValueDTO getDocumentAttributeValueById(Long id) {
        DocumentAttributeValue documentAttributeValue = documentAttributeValueRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Document attribute value not found with: {}", id);
                    return new ResourceNotFoundException("Value doesn't exist");
                });
        log.info("Document Attribute value with id: {} found", id);
        return documentAttributeValueMapper.toDTO(documentAttributeValue);
    }

    @Transactional
    @Override
    public DocumentAttributeValueDTO updateDocumentAttributeValue(Long id, DocumentAttributeValueUpdateDTO value) {
        log.info("Updating DocumentAttributeValue with ID: {}", id);

        DocumentAttributeValue updateValue = documentAttributeValueRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attribute value not found for ID: {}", id);
                    return new ResourceNotFoundException("Attribute value not found: " + id);
                });
        log.debug("Current DocumentAttributeValue details: {}", updateValue);
        updateValue.setValue(value.getValue());
        log.debug("Setting new value: {}", value.getValue());
        updateValue.setAttribute(attributeService.getAttributeMapper().toEntity(attributeService.getAttributeById(value.getAttributeId())));
        log.debug("Setting new attribute with ID: {}", value.getAttributeId());
        updateValue.setDocumentVersion(documentVersionService.getDocumentVersion(value.getDocumentId()));
        log.debug("Setting new document version with ID: {}", value.getDocumentId());
        documentAttributeValueRepository.save(updateValue);
        log.info("DocumentAttributeValue updated successfully: {}", updateValue);
        return documentAttributeValueMapper.toDTO(updateValue);
    }

    @Transactional
    @Override
    public DocumentAttributeValueDTO createDocumentAttributeValue(DocumentAttributeValueCreateDTO value) {    log.info("Creating DocumentAttributeValue for document ID: {}", value.getDocumentId());
        log.info("Creating DocumentAttributeValue for document ID: {}", value.getDocumentId());

        DocumentAttributeValue createValue = DocumentAttributeValue.builder()
                .documentVersion(documentVersionService.getDocumentVersion(value.getDocumentId()))
                .attribute(attributeService.getAttributeMapper().toEntity(attributeService.getAttributeById(value.getAttributeId())))
                .value(value.getValue())
                .build();
        DocumentAttributeValue savedValue = documentAttributeValueRepository.save(createValue);
        log.info("DocumentAttributeValue created successfully with ID: {}", savedValue.getId());
        return documentAttributeValueMapper.toDTO(savedValue);
    }

    @Transactional
    @Override
    public void deleteAttributeValue(Long id) {
        if (!documentAttributeValueRepository.existsById(id)) {
            log.warn("Attribute value not found for ID: {}", id);
            throw new ResourceNotFoundException("Attribute value not found for ID: " + id);
        }

        documentAttributeValueRepository.deleteById(id);
        log.info("Attribute value with ID: {} deleted successfully.", id);
    }
}
