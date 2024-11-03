package ru.caselab.edm.backend.mapper.attribute;

import org.mapstruct.*;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;


@Mapper(componentModel = "spring")
public interface AttributeMapper {

    @Mapping(target = "documentTypeIds", source = "documentTypes")
    AttributeDTO toDTO(Attribute attribute);

    @Mapping(target = "documentTypes", source = "documentTypeIds")
    Attribute toEntity(AttributeDTO attributeDTO);

    default Long mapDocumentTypeToId(DocumentType documentType) {
        return documentType != null ? documentType.getId() : null;
    }

    default DocumentType mapIdToDocumentType(Long id) {
        if (id == null) {
            return null;
        }
        DocumentType documentType = new DocumentType();
        documentType.setId(id);
        return documentType;
    }


}