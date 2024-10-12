package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.AttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.AttributeValueDTO;
import ru.caselab.edm.backend.dto.AttributeValueUpdateDTO;

public interface AttributeValueService {

    AttributeValueDTO getAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId);

    AttributeValueDTO getAttributeValueById(Long id);

    AttributeValueDTO updateAttributeValue(Long id, AttributeValueUpdateDTO value);

    AttributeValueDTO createAttributeValue(AttributeValueCreateDTO value);

    void deleteAttributeValue(Long id);

}
