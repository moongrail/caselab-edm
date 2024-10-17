package ru.caselab.edm.backend.service.impl;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.AttributeMapper;
import ru.caselab.edm.backend.mapper.DocumentTypeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.AttributeService;


import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    @Getter
    private final AttributeMapper attributeMapper;
    private final DocumentTypeRepository documentTypeRepository;


    @Transactional
    @Override
    public AttributeDTO createAttribute(AttributeCreateDTO createAttribute) {

        Attribute attribute = Attribute.builder()
                .dataType(createAttribute.getDataType())
                .name(createAttribute.getName())
                .isRequired(createAttribute.isRequired())
                .documentTypes(mapDocumentTypeIdsToEntities(createAttribute.getDocumentTypeIds()))
                .build();

        return attributeMapper.toDTO(attributeRepository.save(attribute));
    }

    @Transactional
    @Override
    public AttributeDTO getAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));
        return attributeMapper.toDTO(attribute);
    }

    @Override
    public Page<AttributeDTO> getAllAttributes(int page, int size) {
        Page<Attribute> attributes = attributeRepository.findAll(
                PageRequest.of(page, size)
        );
        return attributes.map(attributeMapper::toDTO);
    }

    @Transactional
    @Override
    public AttributeDTO updateAttribute(Long id, AttributeUpdateDTO updateAttributeDTO) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));
        attribute.setName(updateAttributeDTO.getName());
        attribute.setDataType(updateAttributeDTO.getDataType());
        attribute.setRequired(updateAttributeDTO.isRequired());
        attribute.setDocumentTypes(mapDocumentTypeIdsToEntities(updateAttributeDTO.getDocumentTypeIds()));
        attributeRepository.save(attribute);
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public void deleteAttribute(Long id) {
        attributeRepository.deleteById(id);
    }

    private Set<DocumentType> mapDocumentTypeIdsToEntities(Set<Long> documentTypeIds) {
        return new HashSet<>(documentTypeRepository.findAllById(documentTypeIds));
    }

}
