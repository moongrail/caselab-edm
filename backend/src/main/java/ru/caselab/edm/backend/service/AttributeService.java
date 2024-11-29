package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.attribute.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeUpdateDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.attribute.AttributeMapper;

import java.util.List;
import java.util.Set;


public interface AttributeService {
    AttributeDTO createAttribute(AttributeCreateDTO createAttributeDTO);

    void deleteAttribute(Long id);

    AttributeDTO updateAttribute(Long id, AttributeUpdateDTO updateAttributeDTO);

    Page<AttributeDTO> getAllAttributes(int page, int size);

    AttributeDTO getAttributeById(Long id);

    Attribute getAttributeEntityById(Long id);

    AttributeMapper getAttributeMapper();

    List<AttributeSearch> searchByName(String name);

    Set<DocumentVersionDtoWithAuthor> findByNameWithMinLength(String name);
}
