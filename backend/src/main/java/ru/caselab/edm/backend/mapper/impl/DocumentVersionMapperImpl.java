package ru.caselab.edm.backend.mapper.impl;

import jakarta.persistence.OneToMany;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.DocumentVersionPageDto;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.DocumentVersionMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentVersionMapperImpl implements DocumentVersionMapper {

    @Override
    public DocumentVersionDTO toDto(DocumentVersion documentVersion) {
        DocumentVersionDTO documentVersionDTO = new DocumentVersionDTO();
        documentVersionDTO.setDocumentId(documentVersion.getId());
        documentVersionDTO.setDocumentName(documentVersion.getDocumentName());
        documentVersionDTO.setAttributeValues(documentVersion.getDocumentAttributeValue());
        documentVersionDTO.setCreatedAt(documentVersion.getCreatedAt());
        documentVersionDTO.setUpdatedAt(documentVersion.getUpdatedAt());
        documentVersionDTO.setContentUrl(documentVersion.getContentUrl());
        return documentVersionDTO;
    }

    @OneToMany(mappedBy = "documentVersion")
    private List<ApprovementProcessItem> approvementProcessItems;

    @OneToMany(mappedBy = "documentVersion")
    private List<ApprovementProcess> approvementProcesses;


    @Override
    public DocumentVersionPageDto toDtoPage(Page<DocumentVersion> requests) {

        return new DocumentVersionPageDto(requests.getNumber(),
                requests.getSize(), requests.getTotalPages(), requests.getNumberOfElements(),
                toDto(requests.getContent().stream().toList()));
    }

    private List<DocumentVersionDTO> toDto(List<DocumentVersion> versions) {
        return versions.stream().map(this::toDto).toList();
    }
}
