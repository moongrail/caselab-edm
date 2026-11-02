package ru.caselab.edm.security.mapper.role;

import org.mapstruct.Mapper;
import ru.caselab.edm.security.api.dto.role.RoleDTO;
import ru.caselab.edm.security.domain.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDTO toDTO(Role role);
}
