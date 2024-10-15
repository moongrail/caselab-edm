package ru.caselab.edm.backend.mapper;

import org.mapstruct.*;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DocumentTypeMapper {
    DocumentTypeDTO toDto(DocumentType entity);


    DocumentType toEntity(DocumentTypeDTO documentTypeDTO);


}