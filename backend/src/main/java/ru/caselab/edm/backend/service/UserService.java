package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.auth.JwtDTO;
import ru.caselab.edm.backend.dto.auth.LoginUserDTO;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;

import java.util.UUID;

public interface UserService {

    UserPageDTO getAllUsers(int page, int size);

    UserDTO getUserById(UUID id);

    UserDTO createUser(CreateUserDTO createdUser);

    UserDTO updateUser(UUID id, UpdateUserDTO updatedUser);

    void updatePassword(UUID id, UpdatePasswordDTO updatePasswordDTO);

    void deleteUser(UUID id);
    
    JwtDTO auth(LoginUserDTO loginUserDTO);

}
