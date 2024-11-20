package ru.caselab.edm.backend.mapper.documentversion;

import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;

public interface DocumentVersionMapper {
    DocumentVersionDTO toDto(DocumentVersion documentVersion);

    DocumentVersionDtoWithAuthor toDtoWithAuthor(DocumentVersion documentVersion);

    List<DocumentVersionDTO> toDto(List<DocumentVersion> documentVersion);
}
