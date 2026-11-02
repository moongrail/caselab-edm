package ru.caselab.edm.backend.document.api.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.document.api.dto.document.DocumentDTO;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.api.mapper.DocumentAttributeValueMapper;
import ru.caselab.edm.backend.document.api.mapper.DocumentMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentMapperImpl implements DocumentMapper {

    private final DocumentAttributeValueMapper documentAttributeValueMapper;

    @Override
    public DocumentDTO toDto(Document entity) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getOwnerId());
        dto.setDocumentTypeId(entity.getDocumentType().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private List<DocumentDTO> toDto(List<Document> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
