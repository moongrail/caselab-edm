package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentAttributeValueMapper {
    DocumentAttributeValueDTO toDTO(DocumentAttributeValue documentAttributeValue);

    DocumentAttributeValue toEntity(DocumentAttributeValueDTO documentAttributeValueDTO);

    List<DocumentAttributeValueDTO> toDto(List<DocumentAttributeValue> documentAttributeValueList);
}
