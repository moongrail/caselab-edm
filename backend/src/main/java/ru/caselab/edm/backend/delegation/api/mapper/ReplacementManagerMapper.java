package ru.caselab.edm.backend.delegation.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.delegation.api.dto.replacementmanagement.ReplacementManagerDTO;
import ru.caselab.edm.backend.delegation.domain.ReplacementManager;

@Mapper(componentModel = "spring")
public interface ReplacementManagerMapper {

    @Mapping(target = "userId", source = "managerUserId")
    @Mapping(target = "replacementUserId", source = "tempManagerUserId")
    ReplacementManagerDTO toDTO(ReplacementManager replacementManager);

}
