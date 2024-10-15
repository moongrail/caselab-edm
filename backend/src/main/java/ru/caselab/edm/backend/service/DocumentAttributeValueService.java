package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.DocumentAttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;

public interface DocumentAttributeValueService {

    DocumentAttributeValueDTO getDocumentAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId);

    DocumentAttributeValueDTO getDocumentAttributeValueById(Long id);

    DocumentAttributeValueDTO updateDocumentAttributeValue(Long id, DocumentAttributeValueUpdateDTO value);

    DocumentAttributeValueDTO createDocumentAttributeValue(DocumentAttributeValueCreateDTO value);

    void deleteAttributeValue(Long id);

}
