package ru.caselab.edm.backend.mapper;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.dto.DocumentPageDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;

public interface DocumentMapper {
    Document toEntity(DocumentCreateDTO request);
    Document toEntity(DocumentUpdateDTO request);

    DocumentDTO toDto(Document entity);

    DocumentPageDTO toDtoPage(Page<Document> requests);
}
