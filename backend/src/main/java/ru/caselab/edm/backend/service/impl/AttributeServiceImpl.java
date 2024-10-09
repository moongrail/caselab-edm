package ru.caselab.edm.backend.service.impl;

import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.mapper.AttributeMapper;
import ru.caselab.edm.backend.repository.AttributeRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.service.AttributeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;
    private final DocumentTypeRepository documentTypeRepository;

    public AttributeServiceImpl(AttributeRepository attributeRepository, AttributeMapper attributeMapper, DocumentTypeRepository documentTypeRepository) {
        this.attributeRepository = attributeRepository;
        this.attributeMapper = attributeMapper;
        this.documentTypeRepository = documentTypeRepository;
    }

    @Transactional
    @Override
    public AttributeDTO createAttribute(AttributeCreateDTO createAttribute) {
        Attribute attribute = Attribute.builder()
                .dataType(createAttribute.getDataType())
                .name(createAttribute.getName())
                .documentTypes(documentTypeRepository.findAllById(createAttribute.getDocumentTypeIds()))
                .build();
        return attributeMapper.toDTO(attributeRepository.save(attribute));
    }

    @Transactional
    @Override
    public AttributeDTO getAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Attribute not found"));
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public Page<AttributeDTO> getAllAttributes(int page, int size) {
        Page<Attribute> attributes = attributeRepository. findAll(
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
        attribute.setDocumentTypes(documentTypeRepository.findAllById(updateAttributeDTO.getDocumentTypeIds()));
        attributeRepository.save(attribute);
        return attributeMapper.toDTO(attribute);
    }

    @Transactional
    @Override
    public void deleteAttribute(Long id) {
        attributeRepository.deleteById(id);
    }

    @Override
    public AttributeMapper getAttributeMapper() {
        return attributeMapper;
    }


}
