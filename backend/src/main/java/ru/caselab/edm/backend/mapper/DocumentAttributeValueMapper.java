package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentAttributeValueMapper {

    @Mapping(target = "attributeId", source = "attribute")
    @Mapping(target = "documentId", source = "documentVersion")
    DocumentAttributeValueDTO toDTO(DocumentAttributeValue documentAttributeValue);

    DocumentAttributeValue toEntity(DocumentAttributeValueDTO documentAttributeValueDTO);

    List<DocumentAttributeValueDTO> toDto(List<DocumentAttributeValue> documentAttributeValueList);

    default Long mapAttributeToId(Attribute attribute) {
        return attribute != null ? attribute.getId() : null;
    }

    default Long mapDocumentVersionToId(DocumentVersion documentVersion) {
        return documentVersion != null ? documentVersion.getId() : null;
    }
}