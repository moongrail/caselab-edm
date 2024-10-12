package ru.caselab.edm.backend.mapper;
import org.mapstruct.Mapper;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.entity.Attribute;

@Mapper(componentModel = "spring")
public interface AttributeMapper {
    AttributeDTO toDTO(Attribute attribute);
    Attribute toEntity(AttributeDTO attributeDTO);

}
