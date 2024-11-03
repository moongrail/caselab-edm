package ru.caselab.edm.backend.mapper;

import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.entity.Document;

public interface DocumentMapper {

    DocumentDTO toDto(Document entity);
}
