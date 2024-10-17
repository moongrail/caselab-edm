package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.CreateUserDTO;
import ru.caselab.edm.backend.dto.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.UpdateUserDTO;
import ru.caselab.edm.backend.dto.UserDTO;

import java.util.UUID;

public interface UserService {

    Page<UserDTO> getAllUsers(int page, int size);

    UserDTO getUserById(UUID id);

    UserDTO createUser(CreateUserDTO createdUser);

    UserDTO updateUser(UUID id, UpdateUserDTO updatedUser);

    void updatePassword(UUID id, UpdatePasswordDTO updatePasswordDTO);

    void deleteUser(UUID id);

}
