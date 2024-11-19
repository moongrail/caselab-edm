package ru.caselab.edm.backend.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.auth.JwtDTO;
import ru.caselab.edm.backend.dto.auth.LoginUserDTO;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordForAdminDTO;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.Role;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.UserAlreadyExistsException;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.RefreshTokenRepository;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.security.service.RefreshTokenService;
import ru.caselab.edm.backend.service.UserService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserPageDTO getAllUsers(int page, int size) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        log.info("Fetched {} users", users.getTotalElements());
        return userMapper.toPageDTO(users);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserById(UUID id) {
        log.info("Fetching user with id: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            log.info("User with id: {} found", id);
            return userMapper.toDTO(user.get());
        } else {
            log.warn("User not found with id: {}", id);
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }

    @Transactional
    @Override
    public UserDTO createUser(CreateUserDTO createdUser) {
        log.info("Creating a new user with login: {}", createdUser.login());
        if (userRepository.existsByLogin(createdUser.login())) {
            log.warn("User already exists with login: {}", createdUser.login());
            throw new UserAlreadyExistsException("User already exists with this login = %s".formatted(createdUser.login()));
        }
        if (userRepository.existsByEmail(createdUser.email())) {
            log.warn("User already exists with email: {}", createdUser.email());
            throw new UserAlreadyExistsException("User already exists with this email = %s".formatted(createdUser.email()));
        }

        log.info("Trying to find department by user id: {}", createdUser.departmentId());

        Optional<Department> department = departmentRepository.findById(createdUser.departmentId());
        if (department.isEmpty()) {
            log.warn("Department not found with current id: {}", createdUser.departmentId());
            throw new ResourceNotFoundException("Department not found with id = %s".formatted(createdUser.departmentId()));
        }

        Department existingDepartment = department.get();
        Set<Role> roles = new HashSet<>();
        for (RoleName role : createdUser.roles()) {
            Optional<Role> roleOptional = roleRepository.findByName(role);
            if (roleOptional.isPresent()) {
                roles.add(roleOptional.get());
            } else {
                log.warn("Role not found with name: {}", role.name());
                throw new ResourceNotFoundException("Role not found with this name = %s".formatted(role.name()));
            }
        }


        User newUser = User.builder()
                .login(createdUser.login())
                .email(createdUser.email())
                .password(passwordEncoder.encode(createdUser.password()))
                .firstName(createdUser.firstName())
                .lastName(createdUser.lastName())
                .patronymic(createdUser.patronymic())
                .roles(roles)
                .department(existingDepartment)
                .build();

        if (existingDepartment.getMembers() == null)
            existingDepartment.setMembers(new HashSet<>());

        existingDepartment.getMembers().add(newUser);

        departmentRepository.save(existingDepartment);
        userRepository.save(newUser);
        log.info("User created with id: {}", newUser.getId());
        return userMapper.toDTO(newUser);
    }

    @Transactional
    @Override
    public UserDTO updateUser(UUID id, UpdateUserDTO updatedUser) {
        log.info("Updating user with id: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            if (!existingUser.getLogin().equals(updatedUser.login())
                    && userRepository.existsByLogin(updatedUser.login())) {
                log.warn("User already exists with login: {}", updatedUser.login());
                throw new UserAlreadyExistsException("User already exists with this login = %s".formatted(updatedUser.login()));
            }
            if (!existingUser.getEmail().equals(updatedUser.email())
                    && userRepository.existsByEmail(updatedUser.email())) {
                log.warn("User already exists with email: {}", updatedUser.login());
                throw new UserAlreadyExistsException("User already exists with this email = %s".formatted(updatedUser.email()));
            }
            Set<Role> roles = new HashSet<>();
            for (RoleName role : updatedUser.roles()) {
                Optional<Role> roleOptional = roleRepository.findByName(role);
                if (roleOptional.isPresent()) {
                    roles.add(roleOptional.get());
                } else {
                    log.warn("Role not found with name: {}", role.name());
                    throw new ResourceNotFoundException("Role not found with this name = %s".formatted(role.name()));
                }
            }

            existingUser.setLogin(updatedUser.login());
            existingUser.setEmail(updatedUser.email());
            existingUser.setFirstName(updatedUser.firstName());
            existingUser.setLastName(updatedUser.lastName());
            if (updatedUser.patronymic() != null) {
                existingUser.setPatronymic(updatedUser.patronymic());
            }
            existingUser.setRoles(roles);
            userRepository.save(existingUser);
            log.info("User with id: {} successfully updated", id);
            return userMapper.toDTO(existingUser);
        } else {
            log.warn("User not found with id: {}", id);
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }

    }

    @Transactional
    @Override
    public void updatePasswordAsAdmin(UUID userId, UpdatePasswordForAdminDTO updatePasswordForAdminDTO) {
        log.info("Updating password for user with id: {}", userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setPassword(passwordEncoder.encode(updatePasswordForAdminDTO.newPassword()));
            userRepository.save(existingUser);
            refreshTokenRepository.deleteAllByUserId(existingUser.getId());
            log.info("Password successfully updated for user with id: {}", userId);
        } else {
            log.warn("User not found with id: {}", userId);
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(userId));
        }
    }

    @Transactional
    @Override
    public void updatePassword(UUID id, UpdatePasswordDTO updatePasswordDTO) {
        log.info("Updating password for user with id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id %s was not found".formatted(id)));
        if (!passwordEncoder.matches(updatePasswordDTO.oldPassword(), user.getPassword())) {
            log.warn("Invalid old password for user with id: {}", id);
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
        log.info("Deleting user with id: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            log.info("User with id: {} successfully deleted", id);
        } else {
            log.warn("User not found with id: {}", id);
            throw new ResourceNotFoundException("User not found with this id = %s".formatted(id));
        }
    }

    @Transactional
    @Override
    public JwtDTO auth(LoginUserDTO loginUserDTO) {
        log.info("Getting user with login: {}", loginUserDTO.login());
        User existingUser = userRepository.findUserByLogin(loginUserDTO.login()).orElseThrow(() -> new BadCredentialsException("Invalid login or password"));
        log.info("User with login: {} successfully received", loginUserDTO.login());

        log.info("Checking user and DTO for password equality");

        if (passwordEncoder.matches(loginUserDTO.password(), existingUser.getPassword())) {
            log.info("Passwords are equals");
            log.info("Creating JwtDTO");
            JwtDTO jwtDTO = new JwtDTO(existingUser.getId(), refreshTokenService.createRefreshToken(existingUser.getLogin()).getToken(), jwtService.generateToken(new UserInfoDetails(existingUser)));
            log.info("JwtDTO successfully created");

            return jwtDTO;
        } else
            throw new BadCredentialsException("Invalid login or password");
    }
}

