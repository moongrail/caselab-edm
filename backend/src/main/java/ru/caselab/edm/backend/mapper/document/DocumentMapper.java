package ru.caselab.edm.backend.mapper.document;

import ru.caselab.edm.backend.dto.document.DocumentDTO;
import ru.caselab.edm.backend.entity.Document;

public interface DocumentMapper {

    DocumentDTO toDto(Document entity);
}
