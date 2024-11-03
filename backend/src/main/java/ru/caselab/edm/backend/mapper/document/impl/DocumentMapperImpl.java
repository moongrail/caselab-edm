package ru.caselab.edm.backend.mapper.document.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.document.DocumentDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.mapper.DocumentAttributeValueMapper;
import ru.caselab.edm.backend.mapper.document.DocumentMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentMapperImpl implements DocumentMapper {

    private final DocumentAttributeValueMapper documentAttributeValueMapper;

    @Override
    public DocumentDTO toDto(Document entity) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setDocumentTypeId(entity.getDocumentType().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private List<DocumentDTO> toDto(List<Document> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
