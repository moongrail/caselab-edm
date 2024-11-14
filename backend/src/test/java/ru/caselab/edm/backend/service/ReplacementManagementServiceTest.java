package ru.caselab.edm.backend.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.replacementmanagement.UsersForReplacementDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.ReplacementManagementServiceImpl;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ReplacementManagementServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @InjectMocks
    private ReplacementManagementServiceImpl replacementManagementService;


    @Test
    void getUserForReplacement() {
        UUID userId = UUID.randomUUID();
        Department department = new Department();
        department.setId(1L);

        User user = new User();
        user.setId(userId);
        user.setLogin("test.user");
        user.setEmail("test@example.com");
        user.setPatronymic("Test");
        user.setPosition("Manager");
        user.setFirstName("John");
        user.setLastName("Doe");

        List<Department> departments = List.of(department);
        List<User> users = List.of(user);

        Mockito.when(departmentRepository.findDepartmentByManagerUuid(userId)).thenReturn(departments);
        Mockito.when(userRepository.getDepartmentMembersForReplacementManager(department.getId())).thenReturn(users);

        Page<UsersForReplacementDTO> result = replacementManagementService.getAllUsersForReplacementManager(0, 10, userId);

        Assert.assertEquals(1, result.getTotalElements());
    }


    @Test
    public void testGetAllUsersForReplacementDepartmentMember() {
        UUID userId = UUID.randomUUID();
        Department department = new Department();
        department.setId(1L);

        User manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setLogin("test.manager");
        manager.setEmail("manager@example.com");
        manager.setPatronymic("Manager");
        manager.setPosition("Manager");
        manager.setFirstName("Manager");
        manager.setLastName("Manager");

        User member = new User();
        member.setId(UUID.randomUUID());
        member.setLogin("test.member");
        member.setEmail("member@example.com");
        member.setPatronymic("Member");
        member.setPosition("Member");
        member.setFirstName("Member");
        member.setLastName("Member");

        List<Department> departments = List.of(department);

        List<User> managers = List.of(manager);
        List<User> members = List.of(member);

        Mockito.when(departmentRepository.findDepartmentByMemberUuid(userId)).thenReturn(departments);
        Mockito.when(userRepository.getDepartmentManagersForReplacementDepartmentMember(department.getId())).thenReturn(managers);
        Mockito.when(userRepository.getDepartmentMembersForReplacement(userId, department.getId())).thenReturn(members);

        Page<UsersForReplacementDTO> result = replacementManagementService.getAllUsersForReplacementDepartmentMember(0, 10, userId);

        Assert.assertEquals(2, result.getTotalElements());
    }
}