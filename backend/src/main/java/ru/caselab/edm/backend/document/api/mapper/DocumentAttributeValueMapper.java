package ru.caselab.edm.backend.document.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.document.api.dto.attributevalue.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.domain.DocumentAttributeValue;
import ru.caselab.edm.backend.document.domain.DocumentVersion;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentAttributeValueMapper {

    @Mapping(target = "attributeId", source = "attribute")
    DocumentAttributeValueDTO toDTO(DocumentAttributeValue documentAttributeValue);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attribute", source = "attributeId")
    @Mapping(target = "documentVersion", ignore = true)
    DocumentAttributeValue toEntity(DocumentAttributeValueDTO documentAttributeValueDTO);

    List<DocumentAttributeValue> toEntity(List<DocumentAttributeValueDTO> documentAttributeValueDTOList);

    List<DocumentAttributeValueDTO> toDto(List<DocumentAttributeValue> documentAttributeValueList);

    default Long mapAttributeToId(Attribute attribute) {
        return attribute != null ? attribute.getId() : null;
    }

    default Attribute mapIdToAttribute(Long id) {
        if (id == null) {
            return null;
        }
        Attribute attribute = new Attribute();
        attribute.setId(id);
        return attribute;
    }

    default Long mapDocumentVersionToId(DocumentVersion documentVersion) {
        return documentVersion != null ? documentVersion.getId() : null;
    }
}
