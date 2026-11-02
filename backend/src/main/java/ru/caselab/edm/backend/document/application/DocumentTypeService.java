package ru.caselab.edm.backend.document.application;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.document.api.dto.documenttype.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.document.api.dto.documenttype.DocumentTypeDTO;
import ru.caselab.edm.backend.document.api.dto.documenttype.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.document.api.mapper.DocumentTypeMapper;

public interface DocumentTypeService {

    Page<DocumentTypeDTO> getAllDocumentType(int page, int size);

    DocumentTypeDTO getDocumentTypeById(Long id);

    DocumentTypeDTO createDocumentType(DocumentTypeCreateDTO createdDocumentType);

    DocumentTypeDTO updateDocumentType(Long id, DocumentTypeUpdateDTO updateDocumentType);

    void deleteDocumentType(Long id);

    DocumentTypeMapper getDocumentTypeMapper();
}
