package ru.caselab.edm.backend.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.mapper.role.RoleMapper;

import java.util.List;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDTO toDTO(User user);

    default Long map(Department department) {
        return department != null ? department.getId() : null;
    }

    @Mapping(target = "page", source = "number")
    UserPageDTO toPageDTO(Page<User> user);

    List<UserDTO> toListDTO(List<User> users);

}
