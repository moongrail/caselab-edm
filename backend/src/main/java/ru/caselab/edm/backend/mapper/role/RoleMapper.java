package ru.caselab.edm.backend.mapper.role;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.dto.role.RoleDTO;
import ru.caselab.edm.backend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDTO toDTO(Role role);
}
