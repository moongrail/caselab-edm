package ru.caselab.edm.backend.mapper;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.DocumentVersionPageDto;
import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentVersionMapper {
    DocumentVersionDTO toDto(DocumentVersion documentVersion);

    DocumentVersionPageDto toDtoPage(Page<DocumentVersion> requests);

}
