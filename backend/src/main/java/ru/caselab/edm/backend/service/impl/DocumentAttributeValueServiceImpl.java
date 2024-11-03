package ru.caselab.edm.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.document.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.RequieredAttributeEmptyValueException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.DocumentAttributeValueMapper;
import ru.caselab.edm.backend.repository.DocumentAttributeValueRepository;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;
import ru.caselab.edm.backend.service.DocumentVersionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class DocumentAttributeValueServiceImpl implements DocumentAttributeValueService {
    private final DocumentAttributeValueRepository documentAttributeValueRepository;
    private final DocumentAttributeValueMapper documentAttributeValueMapper;

    private final DocumentVersionService documentVersionService;

    public DocumentAttributeValueServiceImpl(DocumentAttributeValueRepository documentAttributeValueRepository,
                                             DocumentAttributeValueMapper documentAttributeValueMapper,
                                             @Lazy DocumentVersionService documentVersionService) {
        this.documentAttributeValueRepository = documentAttributeValueRepository;
        this.documentAttributeValueMapper = documentAttributeValueMapper;
        this.documentVersionService = documentVersionService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentAttributeValueDTO getDocumentAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId) {

        DocumentAttributeValue documentAttributeValue = documentAttributeValueRepository.findByDocumentVersionIdAndAttributeId(documentId, attributeId)
                .orElseThrow(() -> {
                    log.warn("Document attribute value not found with document id: {} and attribute id: {}", documentId, attributeId);
                    return new ResourceNotFoundException("Value doesn't exist");
                });
        log.info("Document Attribute value with document id: {} and attribute id: {} found", documentId, attributeId);
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

    @Override
    @Transactional(readOnly = true)
    public List<DocumentAttributeValueDTO> getDocumentAttributeValuesByDocumentId(Long id) {
        log.info("Getting attribute values by document id: {}", id);
        documentVersionService.getDocumentVersion(id);
        List<DocumentAttributeValue> values = documentAttributeValueRepository.findByDocumentVersionId(id);
        return documentAttributeValueMapper.toDto(values);
    }

    @Transactional
    @Override
    public List<DocumentAttributeValue> updateDocumentAttributeValue(List<DocumentAttributeValueUpdateDTO> value,
                                                                     DocumentVersion documentVersionOld,
                                                                     DocumentVersion documentVersionNew) {
        Long documentVersionOldId = documentVersionOld.getId();

        List<DocumentAttributeValue> oldDocumentAttributeValues = documentAttributeValueRepository
                .findByDocumentVersionId(documentVersionOldId);

        List<DocumentAttributeValue> newDocumentAttributeValueList = new ArrayList<>();

        for (DocumentAttributeValue attribute : oldDocumentAttributeValues) {

            DocumentAttributeValue.DocumentAttributeValueBuilder builder = DocumentAttributeValue.builder()
                    .attribute(attribute.getAttribute())
                    .documentVersion(documentVersionNew);

            Long oldAttributeId = attribute.getAttribute().getId();
            Optional<String> attributeValue = getUpdateAttributeValue(value, oldAttributeId);

            builder.value(attributeValue.orElseGet(attribute::getValue));

            newDocumentAttributeValueList.add(builder.build());

        }
        documentVersionNew.setDocumentAttributeValue(newDocumentAttributeValueList);
        return documentAttributeValueRepository.saveAll(newDocumentAttributeValueList);
    }

    @Override
    public List<DocumentAttributeValue> createDocumentAttributeValues(
            List<AttributeValuetoCreateDocumentDTO> newAttributeValues,
            Document document,
            DocumentVersion documentVersion
    ) {
        Set<Attribute> attributeIds = document.getDocumentType().getAttributes();
        List<DocumentAttributeValue> documentAttributeValueList = new ArrayList<>();

        for (Attribute attribute : attributeIds) {
            Optional<String> attributeValue = getAttributeValue(newAttributeValues, attribute.getId());
            if (attribute.isRequired() && attributeValue.isEmpty()) {
                throw new RequieredAttributeEmptyValueException("Attribute %s is required but not send".formatted(attribute.getName()));
            }
            DocumentAttributeValue documentAttributeValue = DocumentAttributeValue.builder()
                    .attribute(attribute)
                    .documentVersion(documentVersion)
                    .value(attributeValue.get())
                    .build();

            documentAttributeValueList.add(documentAttributeValue);
        }
        return documentAttributeValueRepository.saveAll(documentAttributeValueList);

    }

    private Optional<String> getUpdateAttributeValue(List<DocumentAttributeValueUpdateDTO> newValues, Long attributeId) {
        return newValues
                .stream()
                .filter(value -> attributeId.equals(value.getAttributeId()))
                .map(DocumentAttributeValueUpdateDTO::getValue)
                .findFirst();
    }

    private Optional<String> getAttributeValue(List<AttributeValuetoCreateDocumentDTO> newValues, Long attributeId) {
        return newValues
                .stream()
                .filter(value -> attributeId.equals(value.getAttributeId()))
                .map(AttributeValuetoCreateDocumentDTO::getValue)
                .findFirst();
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
