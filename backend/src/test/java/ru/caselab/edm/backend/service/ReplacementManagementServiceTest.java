package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.dto.role.RoleDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.Role;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.ReplacementManagementServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

class ReplacementManagementServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReplacementManagementServiceImpl departmentService;
    private UUID userId;
    private User user;
    private final Set<RoleDTO> roleDTOS = new HashSet<>();
    private Role role;
    private Department department;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .login("test")
                .email("test@test.ru")
                .firstName("test")
                .lastName("test")
                .patronymic("test")
                .password("test")
                .build();
        roleDTOS.add(new RoleDTO(1L, "USER"));
        role = Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .build();
        department = Department.builder().id(1)
                .name("test").description("test")
                .build();
    }

    @Test
    void getUserForReplacement() {
        Pageable pageable = PageRequest.of(0, 10);

        List<User> userList = new ArrayList<>();
        userList.add(user);
        Page<User> userPage = new PageImpl<>(userList);

        when(userRepository.getAllUsersForReplacement(userId, pageable)).thenReturn(userPage);
        Page<User> result = departmentService.getAllUsersForReplacement(0, 10, userId);

        assertEquals(userPage, result);
    }
}