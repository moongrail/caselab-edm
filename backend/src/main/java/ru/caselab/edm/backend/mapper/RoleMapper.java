package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.dto.RoleDTO;
import ru.caselab.edm.backend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDTO toDTO(Role role);
}
