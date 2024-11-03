package ru.caselab.edm.backend.mapper;

import ru.caselab.edm.backend.dto.DocumentVersionDTO;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;

public interface DocumentVersionMapper {
    DocumentVersionDTO toDto(DocumentVersion documentVersion);

    List<DocumentVersionDTO> toDto(List<DocumentVersion> documentVersion);
}
