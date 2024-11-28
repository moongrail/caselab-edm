package ru.caselab.edm.backend.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.attribute.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.exceptions.AttributeAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.attribute.AttributeMapper;
import ru.caselab.edm.backend.mapper.attribute.AttributeSearchMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.service.AttributeService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    @Getter
    private final AttributeMapper attributeMapper;
    @Getter
    private final AttributeSearchMapper attributeSearchMapper;
    private final DocumentTypeRepository documentTypeRepository;
    private final AttributeSearchRepository attributeSearchRepository;

    @PostConstruct
    public void init() {
        List<Attribute> attributes = attributeRepository.findAll();
        for (Attribute attribute : attributes) {
            AttributeSearch attributeSearch = new AttributeSearch(attribute.getId(),
                    attribute.getName(),
                    attribute.getDataType(),
                    attribute.isRequired());
            attributeSearchRepository.save(attributeSearch);
        }
    }

    @Override
    public List<AttributeSearch> searchByName(String name) {
        log.info("Attribute search with string: {}", name);
        return attributeSearchRepository.findByName(name);
    }

    @Override
    public List<AttributeSearch> findByNameWithMinLength(String name) {
        log.info("Attribute search with string: {}", name);
        return attributeSearchRepository.findByNameWithMinLength(name);
    }

    @Transactional
    @Override
    public AttributeDTO createAttribute(AttributeCreateDTO createAttribute) {
        log.info("Creating attribute with name: {}", createAttribute.getName());

        if (!attributeRepository.findByName(createAttribute.getName()).isEmpty()) {
            throw new AttributeAlreadyExistsException("Attribute with name %s already exists"
                    .formatted(createAttribute.getName()));
        }

        log.debug("Attribute data: {}", createAttribute);
        Attribute attribute = Attribute.builder()
                .dataType(createAttribute.getDataType())
                .name(createAttribute.getName())
                .isRequired(createAttribute.isRequired())
                .build();

        if (createAttribute.getDocumentTypeIds() != null && !createAttribute.getDocumentTypeIds().isEmpty()) {
            attribute.setDocumentTypes(mapDocumentTypeIdsToEntities(createAttribute.getDocumentTypeIds()));
        }

        attributeRepository.save(attribute);
        attributeSearchRepository.save(attributeSearchMapper
                .toDTO(attribute));
        log.info("Attribute created with id: {}", attribute.getId());
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public Attribute getAttributeEntityById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attribute not found with id: {}", id);
                    return new ResourceNotFoundException("Attribute not found with id = %s".formatted(id));

                });
        log.info("Attribute with id: {} found", attribute.getId());
        return attribute;
    }

    @Transactional
    @Override
    public AttributeDTO getAttributeById(Long id) {
        Attribute attribute = getAttributeEntityById(id);
        return attributeMapper.toDTO(attribute);
    }

    @Override
    public Page<AttributeDTO> getAllAttributes(int page, int size) {
        log.info("Fetching all attributes - page: {}, size: {}", page, size);
        Page<Attribute> attributes = attributeRepository.findAll(
                PageRequest.of(page, size)
        );
        log.info("Fetched {} attributes", attributes.getTotalElements());
        return attributes.map(attributeMapper::toDTO);
    }

    @Transactional
    @Override
    public AttributeDTO updateAttribute(Long id, AttributeUpdateDTO updateAttributeDTO) {
        log.info("Updating attribute with ID: {}", id);
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Attribute not found for ID: {}", id);
                    return new ResourceNotFoundException("Attribute not found");
                });
        log.debug("Current attribute details: {}", attribute);

        if (updateAttributeDTO.getName() != null && !updateAttributeDTO.getName().isBlank()) {
            if (!attribute.getName().equals(updateAttributeDTO.getName())
                && !attributeRepository.findByName(updateAttributeDTO.getName()).isEmpty()) {
                log.warn("Attribute already exists with name: {}", updateAttributeDTO.getName());
                throw new AttributeAlreadyExistsException("Attribute with  name: %s is already exist".formatted(updateAttributeDTO.getName()));
            }
            attribute.setName(updateAttributeDTO.getName());
        }

        if (updateAttributeDTO.getDataType() != null && !updateAttributeDTO.getDataType().isBlank()) {
            attribute.setDataType(updateAttributeDTO.getDataType());
        }

        if (updateAttributeDTO.getRequired() != null) {
            attribute.setRequired(updateAttributeDTO.getRequired().booleanValue());
        }

        log.debug("Updating document types for attribute with ID: {}", id);

        if (updateAttributeDTO.getDocumentTypeIds() != null && !updateAttributeDTO.getDocumentTypeIds().isEmpty()) {
            attribute.setDocumentTypes(mapDocumentTypeIdsToEntities(updateAttributeDTO.getDocumentTypeIds()));
        } else {
            log.debug("No document type IDs provided for update. Skipping document type update.");
        }
        attributeRepository.save(attribute);
        log.info("Attribute updated successfully: {}", attribute);
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public void deleteAttribute(Long id) {
        Optional<Attribute> attribute = attributeRepository.findById(id);
        if (attribute.isPresent()) {
            attributeRepository.delete(attribute.get());
            attributeSearchRepository.delete(attributeSearchMapper.toDTO(attribute.get()));
        } else {
            throw new ResourceNotFoundException("Attribute not found with this id = %s".formatted(id));
        }
    }

    private Set<DocumentType> mapDocumentTypeIdsToEntities(Set<Long> documentTypeIds) {
        log.debug("Mapping DocumentType IDs to entities: {}", documentTypeIds);
        Set<DocumentType> documentTypes = new HashSet<>();
        for (Long id : documentTypeIds) {
            DocumentType documentType = documentTypeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("DocumentType not found for ID: {}", id);
                        return new ResourceNotFoundException("DocumentType not found for ID: " + id);
                    });
            documentTypes.add(documentType);
        }
        log.debug("Successfully mapped {} DocumentType(s) to entities.", documentTypes.size());

        return documentTypes;
    }

}
