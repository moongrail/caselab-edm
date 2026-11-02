package ru.caselab.edm.backend.document.application;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.document.api.dto.attribute.AttributeCreateDTO;
import ru.caselab.edm.backend.document.api.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.document.api.dto.attribute.AttributeUpdateDTO;
import ru.caselab.edm.backend.document.api.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.document.api.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.integration.search.AttributeSearch;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.api.mapper.AttributeMapper;

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
