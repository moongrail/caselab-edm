package ru.caselab.edm.backend.document.application;

import ru.caselab.edm.backend.document.api.dto.attributevalue.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.document.api.dto.attributevalue.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.document.api.dto.attributevalue.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentAttributeValue;
import ru.caselab.edm.backend.document.domain.DocumentVersion;

import java.util.List;


public interface DocumentAttributeValueService {

    DocumentAttributeValueDTO getDocumentAttributeValueByDocumentAndAttribute(Long documentId, Long attributeId);

    DocumentAttributeValueDTO getDocumentAttributeValueById(Long id);

    List<DocumentAttributeValueDTO> getDocumentAttributeValuesByDocumentId(Long id);

    //DocumentAttributeValueDTO updateDocumentAttributeValue(Long id, DocumentAttributeValueUpdateDTO value);

    List<DocumentAttributeValue> updateDocumentAttributeValue(List<DocumentAttributeValueUpdateDTO> value,
                                                              DocumentVersion documentVersionOld,
                                                              DocumentVersion documentVersionNew);


    /*    DocumentAttributeValueDTO createDocumentAttributeValue(DocumentAttributeValueCreateDTO value);*/

    List<DocumentAttributeValue> createDocumentAttributeValues(List<AttributeValuetoCreateDocumentDTO> document, Document newDocument, DocumentVersion newDocumentVersion);

    /*    List<DocumentAttributeValue> createDocumentAttributeValue(List<DocumentAttributeValueCreateDTO> value);*/


    void deleteAttributeValue(Long id);

}
