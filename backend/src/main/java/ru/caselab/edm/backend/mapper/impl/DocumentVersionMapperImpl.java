package ru.caselab.edm.backend.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.DocumentVersionDTO;
import ru.caselab.edm.backend.dto.DocumentVersionPageDto;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.DocumentVersionMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DocumentVersionMapperImpl implements DocumentVersionMapper {

    @Override
    public DocumentVersionDTO toDto(DocumentVersion documentVersion) {

/*        return new DocumentVersionDTO(documentVersion.getId(),
                documentVersion.getDocumentName(),
                documentVersion.getCreatedAt(),
                documentVersion.getUpdatedAt(),
                documentVersion.getContentUrl(),
                documentVersion.getDocument().getId());*/
        return null;
    }



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
