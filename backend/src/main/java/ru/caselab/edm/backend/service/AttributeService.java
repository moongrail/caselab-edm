package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;
import ru.caselab.edm.backend.mapper.AttributeMapper;


public interface AttributeService {
    AttributeDTO createAttribute(AttributeCreateDTO createAttributeDTO);

    void deleteAttribute(Long id);

    AttributeDTO updateAttribute(Long id, AttributeUpdateDTO updateAttributeDTO);

    Page<AttributeDTO> getAllAttributes(int page, int size);

    AttributeDTO getAttributeById(Long id);

    AttributeMapper getAttributeMapper();

}
