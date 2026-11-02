package ru.caselab.edm.backend.document.api.mapper;

import ru.caselab.edm.backend.document.api.dto.document.DocumentDTO;
import ru.caselab.edm.backend.document.domain.Document;

public interface DocumentMapper {

    DocumentDTO toDto(Document entity);
}
