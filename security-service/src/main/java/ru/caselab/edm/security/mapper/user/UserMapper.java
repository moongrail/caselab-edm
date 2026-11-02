package ru.caselab.edm.security.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.caselab.edm.security.api.dto.user.UserDTO;
import ru.caselab.edm.security.api.dto.user.UserPageDTO;
import ru.caselab.edm.security.domain.Department;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.mapper.role.RoleMapper;

import java.util.List;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDTO toDTO(User user);

    default Long map(Department department) {
        return department != null ? department.getId() : null;
    }

    UserPageDTO toPageDTO(Page<User> user);

    List<UserDTO> toListDTO(List<User> users);

}
