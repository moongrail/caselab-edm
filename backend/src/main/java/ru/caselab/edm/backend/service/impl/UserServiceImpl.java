package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordForAdminDTO;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.Role;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.UserAlreadyExistsException;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.RefreshTokenRepository;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.UserService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    @Override
    public UserPageDTO getAllUsers(int page, int size) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        return userMapper.toPageDTO(users);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserById(UUID id) {
        return userMapper.toDTO(findUser(id));
    }

    @Transactional
    @Override
    public UserDTO createUser(CreateUserDTO createdUser) {
        log.info("Creating a new user with login: {}", createdUser.login());
        if (userRepository.existsByLogin(createdUser.login())) {
            throw new UserAlreadyExistsException(
                    "User already exists with this login = %s".formatted(createdUser.login()));
        }
        if (userRepository.existsByEmail(createdUser.email())) {
            throw new UserAlreadyExistsException(
                    "User already exists with this email = %s".formatted(createdUser.email()));
        }

        Department department = departmentRepository.findById(createdUser.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id = %s".formatted(createdUser.departmentId())));

        User newUser = User.builder()
                .login(createdUser.login())
                .email(createdUser.email())
                .password(passwordEncoder.encode(createdUser.password()))
                .firstName(createdUser.firstName())
                .lastName(createdUser.lastName())
                .patronymic(createdUser.patronymic())
                .position(createdUser.position())
                .roles(resolveRoles(createdUser.roles()))
                .department(department)
                .build();

        if (department.getMembers() == null) {
            department.setMembers(new HashSet<>());
        }
        department.getMembers().add(newUser);

        userRepository.save(newUser);
        departmentRepository.save(department);
        log.info("User created with id: {}", newUser.getId());
        return userMapper.toDTO(newUser);
    }

    @Transactional
    @Override
    public UserDTO updateUser(UUID id, UpdateUserDTO updatedUser) {
        log.info("Updating user with id: {}", id);
        User existingUser = findUser(id);

        if (updatedUser.login() != null && !updatedUser.login().isBlank()) {
            if (!existingUser.getLogin().equals(updatedUser.login())
                    && userRepository.existsByLogin(updatedUser.login())) {
                throw new UserAlreadyExistsException(
                        "User already exists with this login = %s".formatted(updatedUser.login()));
            }
            existingUser.setLogin(updatedUser.login());
        }

        if (updatedUser.email() != null && !updatedUser.email().isBlank()) {
            if (!existingUser.getEmail().equals(updatedUser.email())
                    && userRepository.existsByEmail(updatedUser.email())) {
                throw new UserAlreadyExistsException(
                        "User already exists with this email = %s".formatted(updatedUser.email()));
            }
            existingUser.setEmail(updatedUser.email());
        }

        if (updatedUser.firstName() != null && !updatedUser.firstName().isBlank()) {
            existingUser.setFirstName(updatedUser.firstName());
        }
        if (updatedUser.lastName() != null && !updatedUser.lastName().isBlank()) {
            existingUser.setLastName(updatedUser.lastName());
        }
        if (updatedUser.patronymic() != null) {
            existingUser.setPatronymic(updatedUser.patronymic());
        }
        if (updatedUser.position() != null) {
            existingUser.setPosition(updatedUser.position());
        }
        if (updatedUser.roles() != null) {
            existingUser.setRoles(resolveRoles(updatedUser.roles()));
        }

        userRepository.save(existingUser);
        log.info("User with id: {} successfully updated", id);
        return userMapper.toDTO(existingUser);
    }

    @Transactional
    @Override
    public void updatePasswordAsAdmin(UUID userId, UpdatePasswordForAdminDTO updatePasswordForAdminDTO) {
        User user = findUser(userId);
        user.setPassword(passwordEncoder.encode(updatePasswordForAdminDTO.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUserId(user.getId());
        log.info("Password successfully updated for user with id: {}", userId);
    }

    @Transactional
    @Override
    public void updatePassword(UUID id, UpdatePasswordDTO updatePasswordDTO) {
        User user = findUser(id);
        if (!passwordEncoder.matches(updatePasswordDTO.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUserId(user.getId());
        log.info("Password successfully updated for user with id: {}", id);
    }

    @Transactional
    @Override
    public void deleteUser(UUID id) {
        userRepository.delete(findUser(id));
        log.info("User with id: {} successfully deleted", id);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with this id = %s".formatted(id)));
    }

    private Set<Role> resolveRoles(RoleName[] roleNames) {
        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role not found with this name = %s".formatted(roleName)));
            roles.add(role);
        }
        return roles;
    }
}
