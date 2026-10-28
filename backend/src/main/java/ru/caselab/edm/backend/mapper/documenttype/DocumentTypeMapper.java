package ru.caselab.edm.backend.mapper.documenttype;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.documenttype.DocumentTypeDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;

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
