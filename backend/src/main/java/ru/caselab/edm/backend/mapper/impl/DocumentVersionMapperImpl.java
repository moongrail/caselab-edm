package ru.caselab.edm.backend.mapper.impl;

import jakarta.persistence.OneToMany;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.dto.documentversion.DocumentVersionDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
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
        return documentVersionDTO;
    }

    @OneToMany(mappedBy = "documentVersion")
    private List<ApprovementProcessItem> approvementProcessItems;

    @OneToMany(mappedBy = "documentVersion")
    private List<ApprovementProcess> approvementProcesses;

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
            dto.setAttributeValues(toAttrDtos(v.getDocumentAttributeValue()));
            documentVersionDTOList.add(dto);
        }
        return documentVersionDTOList;
    }

    private List<DocumentAttributeValueDTO> toAttrDtos(List<DocumentAttributeValue> entityList) {
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
