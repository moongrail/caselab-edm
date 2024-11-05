package ru.caselab.edm.backend.mapper.user;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.mapper.role.RoleMapper;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDTO toDTO(User user);

    UserPageDTO toPageDTO(Page<User> user);

}
