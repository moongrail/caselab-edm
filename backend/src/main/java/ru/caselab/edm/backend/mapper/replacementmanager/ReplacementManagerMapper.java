package ru.caselab.edm.backend.mapper.replacementmanager;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.replacementmanagement.ReplacementManagerDTO;
import ru.caselab.edm.backend.entity.ReplacementManager;

@Mapper(componentModel = "spring")
public interface ReplacementManagerMapper {

    @Mapping(target = "userId", source = "managerUser.id")
    @Mapping(target = "replacementUserId", source = "tempManagerUser.id")
    ReplacementManagerDTO toDTO(ReplacementManager replacementManager);

}
