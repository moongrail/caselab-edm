package ru.caselab.edm.backend.mapper.attribute;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;

@Mapper(componentModel = "spring")
public interface AttributeSearchMapper {
    AttributeSearch toDTO(Attribute attribute);

    Attribute toEntity(AttributeSearch attributeSearch);
}
