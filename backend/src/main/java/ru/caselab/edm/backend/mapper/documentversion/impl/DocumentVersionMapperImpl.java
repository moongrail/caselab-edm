package ru.caselab.edm.backend.mapper.documentversion.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDtoWithAuthor;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.documentversion.DocumentVersionMapper;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentVersionMapperImpl implements DocumentVersionMapper {

    @Override
    public DocumentVersionDTO toDto(DocumentVersion documentVersion) {
        DocumentVersionDTO documentVersionDTO = new DocumentVersionDTO();
        documentVersionDTO.setId(documentVersion.getId());
        documentVersionDTO.setDocumentId(documentVersion.getDocument().getId());
        documentVersionDTO.setDocumentName(documentVersion.getDocumentName());
        documentVersionDTO.setAttributeValues(toAttrDtos(documentVersion.getDocumentAttributeValue()));
        documentVersionDTO.setCreatedAt(documentVersion.getCreatedAt());
        documentVersionDTO.setContentUrl(documentVersion.getContentUrl());
        documentVersionDTO.setState(documentVersion.getStatus());
        return documentVersionDTO;
    }

    @Override
    public DocumentVersionDtoWithAuthor toDtoWithAuthor(DocumentVersion documentVersion) {
        DocumentVersionDtoWithAuthor documentVersionDtoWithAuthor = new DocumentVersionDtoWithAuthor();
        documentVersionDtoWithAuthor.setId(documentVersion.getId());
        documentVersionDtoWithAuthor.setDocumentId(documentVersion.getDocument().getId());
        documentVersionDtoWithAuthor.setDocumentName(documentVersion.getDocumentName());
        documentVersionDtoWithAuthor.setAttributeValues(toAttrDtos(documentVersion.getDocumentAttributeValue()));
        documentVersionDtoWithAuthor.setCreatedAt(documentVersion.getCreatedAt());
        documentVersionDtoWithAuthor.setContentUrl(documentVersion.getContentUrl());
        documentVersionDtoWithAuthor.setState(documentVersion.getStatus());
        documentVersionDtoWithAuthor.setAuthor(documentVersion.getDocument().getUser().getLogin());

        return documentVersionDtoWithAuthor;
    }

    @Override
    public List<DocumentVersionDTO> toDto(List<DocumentVersion> versions) {
        List<DocumentVersionDTO> documentVersionDTOList = new ArrayList<>();
        for (DocumentVersion v : versions) {
            DocumentVersionDTO dto = new DocumentVersionDTO();
            dto.setDocumentId(v.getDocument().getId());
            dto.setDocumentName(v.getDocumentName());
            dto.setId(v.getId());
            dto.setCreatedAt(v.getCreatedAt());
            dto.setContentUrl(v.getContentUrl());
            dto.setState(v.getStatus());
            dto.setAttributeValues(toAttrDtos(v.getDocumentAttributeValue()));
            documentVersionDTOList.add(dto);
        }
        return documentVersionDTOList;
    }

    private List<DocumentAttributeValueDTO> toAttrDtos(List<DocumentAttributeValue> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        return entityList.stream()
                .map(this::toAttrDtos)
                .toList();
    }

    private DocumentAttributeValueDTO toAttrDtos(DocumentAttributeValue entity) {
        DocumentAttributeValueDTO dto = new DocumentAttributeValueDTO();
        dto.setAttributeId(entity.getAttribute().getId());
        dto.setValue(entity.getValue());
        return dto;
    }
}