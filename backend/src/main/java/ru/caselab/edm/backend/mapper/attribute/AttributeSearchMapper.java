package ru.caselab.edm.backend.mapper.attribute;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;

@Mapper(componentModel = "spring")
public interface AttributeSearchMapper {

    default AttributeSearch toDTO(Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        return AttributeSearch.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .dataType(attribute.getDataType())
                .isRequired(attribute.isRequired())
                .build();
    }

    default Attribute toEntity(AttributeSearch attributeSearch) {
        if (attributeSearch == null) {
            return null;
        }
        return Attribute.builder()
                .id(attributeSearch.getId())
                .name(attributeSearch.getName())
                .dataType(attributeSearch.getDataType())
                .isRequired(attributeSearch.isRequired())
                .build();
    }
}
