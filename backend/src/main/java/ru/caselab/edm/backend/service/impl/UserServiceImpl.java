package ru.caselab.edm.backend.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
                .password(passwordEncoder.encode(createdUser.password()))
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
            User existingUser = user.get();
            if (!existingUser.getLogin().equals(updatedUser.login())
                    && userRepository.existsByLogin(updatedUser.login())) {
                throw new UserAlreadyExistsException("User already exists with this login = %s".formatted(updatedUser.login()));
            }
            if (!existingUser.getEmail().equals(updatedUser.email())
                    && userRepository.existsByEmail(updatedUser.email())) {
                throw new UserAlreadyExistsException("User already exists with this email = %s".formatted(updatedUser.email()));
            }
            existingUser.setLogin(updatedUser.login());
            existingUser.setEmail(updatedUser.email());
            existingUser.setFirstName(updatedUser.firstName());
            existingUser.setLastName(updatedUser.lastName());
            if (updatedUser.patronymic() != null) {
                existingUser.setPatronymic(updatedUser.patronymic());
            }
            userRepository.save(existingUser);
            return userMapper.toDTO(existingUser);
        } else {
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }

    @Transactional
    @Override
    public void updatePassword(UUID id, UpdatePasswordDTO updatePasswordDTO) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            if (!passwordEncoder.matches(updatePasswordDTO.oldPassword(), existingUser.getPassword())) {
                throw new BadCredentialsException("Invalid old password");
            }
            existingUser.setPassword(passwordEncoder.encode(updatePasswordDTO.newPassword()));
            userRepository.save(existingUser);
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
