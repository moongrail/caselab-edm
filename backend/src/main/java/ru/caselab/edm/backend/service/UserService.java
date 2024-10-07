package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.CreateUserDTO;
import ru.caselab.edm.backend.dto.UpdateUserDTO;
import ru.caselab.edm.backend.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDTO> getAllUsers(int page, int size);

    UserDTO getUserById(UUID id);

    UserDTO createUser(CreateUserDTO createdUser);

    UserDTO updateUser(UUID id, UpdateUserDTO updatedUser);

    void deleteUser(UUID id);

}
