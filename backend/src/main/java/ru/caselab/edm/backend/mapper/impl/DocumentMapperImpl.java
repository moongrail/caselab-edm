package ru.caselab.edm.backend.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.dto.DocumentPageDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.mapper.AttributeValueMapper;
import ru.caselab.edm.backend.mapper.DocumentMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentMapperImpl implements DocumentMapper {

    private final AttributeValueMapper documentAttributeValueMapper;
    @Override
    public DocumentDTO toDto(Document entity) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUserId(entity.getUser().getId());
        dto.setDocumentTypeId(entity.getDocumentType().getId());
        dto.setUpdateDate(entity.getUpdateDate());
        dto.setCreationDate(entity.getCreationDate());
        dto.setData(entity.getData());
        dto.setDocumentAttributeValues(documentAttributeValueMapper.toDto(entity.getDocumentAttributeValues()));
        return dto;
    }

    @Override
    public DocumentPageDTO toDtoPage(Page<Document> requests) {
        DocumentPageDTO dto = new DocumentPageDTO();

        dto.setPage(requests.getNumber());
        dto.setSize(requests.getSize());
        dto.setTotalPages(requests.getTotalPages());
        dto.setTotalElements(requests.getNumberOfElements());
        dto.setContent(toDto(requests.getContent().stream().toList()));

        return dto;
    }

    private List<DocumentDTO> toDto(List<Document> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
