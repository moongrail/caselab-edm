package ru.caselab.edm.backend.service.impl;

import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.mapper.AttributeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.service.AttributeService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Data
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    public AttributeServiceImpl(AttributeRepository attributeRepository, AttributeMapper attributeMapper) {
        this.attributeRepository = attributeRepository;
        this.attributeMapper = attributeMapper;
    }

    @Transactional
    @Override
    public AttributeDTO createAttribute(AttributeCreateDTO createAttributeDTO) {
        Attribute attribute = Attribute.builder()
                .dataType(createAttributeDTO.getDataType())
                .name(createAttributeDTO.getName())
                .documentTypes(documentTypeRepository.findAllById(createAttributeDTO.getDocumentTypeIds()))
                .build();
        return attributeMapper.toDTO(attributeRepository.save(attribute));
    }

    @Transactional
    @Override
    public AttributeDTO getAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Attribute not found"));
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public List<AttributeDTO> getAllAttributes() {
        return attributeRepository.findAll().stream()
                .map(attributeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AttributeDTO updateAttribute(Long id, AttributeUpdateDTO updateAttributeDTO) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Attribute not found"));
        attribute.setName(updateAttributeDTO.getName());
        attribute.setDataType(updateAttributeDTO.getDataType());
        attribute.setDocumentTypes(documentTypeRepository.findAllById(updateAttributeDTO.getDocumentTypeIds()));
        attributeRepository.save(attribute);
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public void deleteAttribute(Long id) {
        attributeRepository.deleteById(id);
    }




}
