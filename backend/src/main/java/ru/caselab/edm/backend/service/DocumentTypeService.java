package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;

public interface DocumentTypeService {

    Page<DocumentTypeDTO> getAllDocumentType(int page, int size);

    DocumentTypeDTO getDocumentTypeById(Long id);

    DocumentTypeDTO createDocumentType(DocumentTypeCreateDTO createdDocumentType);

    DocumentTypeDTO updateDocumentType(Long id, DocumentTypeUpdateDTO updateDocumentType);

    void deleteDocumentType(Long id);
}
