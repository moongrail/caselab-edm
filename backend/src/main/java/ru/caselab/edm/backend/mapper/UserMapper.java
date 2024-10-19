package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.UserDTO;
import ru.caselab.edm.backend.dto.UserPageDTO;
import ru.caselab.edm.backend.entity.User;


@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDTO toDTO(User user);
    UserPageDTO toPageDTO(Page<User> user);

}
