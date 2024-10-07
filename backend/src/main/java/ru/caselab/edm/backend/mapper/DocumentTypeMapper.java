package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.model.DocumentType;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class DocumentTypeMapper {
    public abstract DocumentTypeDTO map(DocumentType model);
}
