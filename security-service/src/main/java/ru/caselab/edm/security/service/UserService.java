package ru.caselab.edm.security.service;

import ru.caselab.edm.security.api.dto.user.CreateUserDTO;
import ru.caselab.edm.security.api.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.security.api.dto.user.UpdatePasswordForAdminDTO;
import ru.caselab.edm.security.api.dto.user.UpdateUserDTO;
import ru.caselab.edm.security.api.dto.user.UserDTO;
import ru.caselab.edm.security.api.dto.user.UserPageDTO;

import java.util.UUID;

public interface UserService {

    UserPageDTO getAllUsers(int page, int size);

    UserDTO getUserById(UUID id);

    UserDTO createUser(CreateUserDTO createdUser);

    UserDTO updateUser(UUID id, UpdateUserDTO updatedUser);

    void updatePassword(UUID userId, UpdatePasswordDTO updatePasswordDTO);

    void updatePasswordAsAdmin(UUID userId, UpdatePasswordForAdminDTO updatePasswordForAdminDTO);

    void deleteUser(UUID id);
}
