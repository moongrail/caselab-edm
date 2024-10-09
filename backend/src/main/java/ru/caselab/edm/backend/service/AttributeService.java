package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;

import java.util.List;

public interface AttributeService {
    AttributeDTO createAttribute(AttributeCreateDTO createAttributeDTO);
    void deleteAttribute(Long id);
    AttributeDTO updateAttribute(Long id, AttributeUpdateDTO updateAttributeDTO);
    List<AttributeDTO> getAllAttributes();
    AttributeDTO getAttributeById(Long id);

}
