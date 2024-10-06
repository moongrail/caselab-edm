package ru.caselab.edm.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.CreateUserDTO;
import ru.caselab.edm.backend.dto.UpdateUserDTO;
import ru.caselab.edm.backend.dto.UserDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
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
            throw new ResourceNotFoundException("User not found");
        }
    }

    @Transactional
    @Override
    public UserDTO createUser(CreateUserDTO createdUser) {
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
            exisingUser.setLogin(updatedUser.login());
            exisingUser.setEmail(updatedUser.email());
            exisingUser.setFirstName(updatedUser.firstName());
            exisingUser.setLastName(updatedUser.lastName());
            exisingUser.setPatronymic(updatedUser.patronymic());
            userRepository.save(exisingUser);
            return userMapper.toDTO(exisingUser);
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }

    @Override
    public void deleteUser(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
        } else {
            throw new ResourceNotFoundException("User not found");
        }
    }
}
