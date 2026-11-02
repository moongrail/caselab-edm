package ru.caselab.edm.backend.document.api.mapper;

import ru.caselab.edm.backend.document.api.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.document.api.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.document.domain.DocumentVersion;

import java.util.List;

public interface DocumentVersionMapper {
    DocumentVersionDTO toDto(DocumentVersion documentVersion);

    DocumentVersionDtoWithAuthor toDtoWithAuthor(DocumentVersion documentVersion, String author);

    List<DocumentVersionDTO> toDto(List<DocumentVersion> documentVersion);
}