package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.AttributeValueDTO;
import ru.caselab.edm.backend.entity.AttributeValue;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttributeValueMapper {
    @Mapping(source = "attribute.id", target = "attributeId")
    @Mapping(source = "document.id", target = "documentId")
    AttributeValueDTO toDTO(AttributeValue attributeValue);

    @Mapping(source = "attributeId", target = "attribute.id")
    @Mapping(source = "documentId", target = "document.id")
    AttributeValue toEntity(AttributeValueDTO attributeValueDTO);

    @Mapping(source = "attribute.id", target = "attributeId")
    @Mapping(source = "document.id", target = "documentId")
    List<AttributeValueDTO> toDto(List<AttributeValue> attributeValueDTOList);
}
