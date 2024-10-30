package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;

public interface DocumentAttributeValueService {

    DocumentAttributeValueDTO getDocumentAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId);

    DocumentAttributeValueDTO getDocumentAttributeValueById(Long id);

    List<DocumentAttributeValueDTO> getDocumentAttributeValuesByDocumentId(Long id);

    DocumentAttributeValueDTO updateDocumentAttributeValue(Long id, DocumentAttributeValueUpdateDTO value);

/*    DocumentAttributeValueDTO createDocumentAttributeValue(DocumentAttributeValueCreateDTO value);*/

    List<DocumentAttributeValue> createDocumentAttributeValues(List<AttributeValuetoCreateDocumentDTO> document, Document newDocument, DocumentVersion newDocumentVersion);

/*    List<DocumentAttributeValue> createDocumentAttributeValue(List<DocumentAttributeValueCreateDTO> value);*/

    void deleteAttributeValue(Long id);

}
