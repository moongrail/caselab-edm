package ru.caselab.edm.backend.document.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.document.api.dto.documenttype.DocumentTypeDTO;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.domain.DocumentType;

@Mapper(componentModel = "spring")
public interface DocumentTypeMapper {

    @Mapping(target = "attributeIds", source = "attributes")
    DocumentTypeDTO toDto(DocumentType entity);

    @Mapping(target = "attributes", source = "attributeIds")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "documents", ignore = true)
    DocumentType toEntity(DocumentTypeDTO documentTypeDTO);

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
}
