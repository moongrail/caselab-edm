package ru.caselab.edm.backend.service.impl;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import ru.caselab.edm.backend.dto.CreateUserDTO;
import ru.caselab.edm.backend.dto.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.UpdateUserDTO;
import ru.caselab.edm.backend.dto.UserDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.UserAlreadyExistsException;
import ru.caselab.edm.backend.mapper.UserMapper;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDTO> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size)).get()
                .map(userMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return userMapper.toDTO(user.get());
        } else {
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }

    @Transactional
    @Override
    public UserDTO createUser(CreateUserDTO createdUser) {
        if (userRepository.existsByLogin(createdUser.login())) {
            throw new UserAlreadyExistsException("User already exists with this login = %s".formatted(createdUser.login()));
        }
        if (userRepository.existsByEmail(createdUser.email())) {
            throw new UserAlreadyExistsException("User already exists with this email = %s".formatted(createdUser.email()));
        }
        User newUser = User.builder()
                .login(createdUser.login())
                .email(createdUser.email())
                .password(createdUser.password())
                .firstName(createdUser.firstName())
                .lastName(createdUser.lastName())
                .patronymic(createdUser.patronymic())
                .build();
        userRepository.save(newUser);
        return userMapper.toDTO(newUser);
    }

    @Transactional
    @Override
    public UserDTO updateUser(UUID id, UpdateUserDTO updatedUser) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User exisingUser = user.get();
            if (!exisingUser.getLogin().equals(updatedUser.login())
                    && userRepository.existsByLogin(updatedUser.login())) {
                throw new UserAlreadyExistsException("User already exists with this login = %s".formatted(updatedUser.login()));
            }
            if (!exisingUser.getEmail().equals(updatedUser.email())
                    && userRepository.existsByEmail(updatedUser.email())) {
                throw new UserAlreadyExistsException("User already exists with this email = %s".formatted(updatedUser.email()));
            }
            exisingUser.setLogin(updatedUser.login());
            exisingUser.setEmail(updatedUser.email());
            exisingUser.setFirstName(updatedUser.firstName());
            exisingUser.setLastName(updatedUser.lastName());
            exisingUser.setPatronymic(updatedUser.patronymic());
            userRepository.save(exisingUser);
            return userMapper.toDTO(exisingUser);
        } else {
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }

    @Transactional
    @Override
    public void updatePassword(UUID id, UpdatePasswordDTO updatePasswordDTO) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User exisingUser = user.get();
            exisingUser.setPassword(updatePasswordDTO.newPassword());
            userRepository.save(exisingUser);
        } else {
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }

    @Transactional
    @Override
    public void deleteUser(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
        } else {
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }
}
