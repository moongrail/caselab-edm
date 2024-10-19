package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.dto.UserDTO;
import ru.caselab.edm.backend.entity.User;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDTO toDTO(User user);

}
