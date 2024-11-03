package ru.caselab.edm.backend.mapper.attributevalue;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentAttributeValueMapper {

    @Mapping(target = "attributeId", source = "attribute")
    DocumentAttributeValueDTO toDTO(DocumentAttributeValue documentAttributeValue);

    DocumentAttributeValue toEntity(DocumentAttributeValueDTO documentAttributeValueDTO);

    List<DocumentAttributeValue> toEntity(List<DocumentAttributeValueDTO> documentAttributeValueDTOList);

    List<DocumentAttributeValueDTO> toDto(List<DocumentAttributeValue> documentAttributeValueList);

    default Long mapAttributeToId(Attribute attribute) {
        return attribute != null ? attribute.getId() : null;
    }

    default Long mapDocumentVersionToId(DocumentVersion documentVersion) {
        return documentVersion != null ? documentVersion.getId() : null;
    }
}