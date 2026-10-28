package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.department.CreateDepartmentDTO;
import ru.caselab.edm.backend.dto.department.DepartmentDTO;
import ru.caselab.edm.backend.dto.department.DepartmentPageDTO;
import ru.caselab.edm.backend.dto.department.MembersDTO;
import ru.caselab.edm.backend.dto.department.StatisticMembersDTO;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ManagerOfAnotherDepartmentException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.department.DepartmentMapper;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.DepartmentServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.THIRD_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private User manager;
    private User member;
    private Department department;

    @BeforeEach
    void setUp() {
        manager = user(OWNER_ID, "manager");
        member = user(SIGNER_ID, "member");
        department = Department.builder()
                .id(5L)
                .name("Development")
                .description("Description")
                .members(new HashSet<>())
                .build();
    }

    @Test
    void createDepartment_withoutParent_savesDepartment() {
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", 0L, OWNER_ID.toString());
        DepartmentDTO expected = new DepartmentDTO(5L, "Development", "Description", 0L);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(manager));
        when(userRepository.existsUserAsManager(OWNER_ID)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });
        when(departmentMapper.toDto(any(Department.class))).thenReturn(expected);

        DepartmentDTO result = departmentService.createDepartment(request);

        assertThat(result).isSameAs(expected);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_withNullParent_doesNotResolveParentDepartment() {
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", null, OWNER_ID.toString());
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(manager));
        when(userRepository.existsUserAsManager(OWNER_ID)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });
        when(departmentMapper.toDto(any(Department.class))).thenReturn(
                new DepartmentDTO(5L, "Development", "Description", null));

        DepartmentDTO result = departmentService.createDepartment(request);

        assertThat(result.parentId()).isNull();
        assertThat(manager.getDepartment()).isNull();
        verify(departmentRepository, never()).findById(anyLong());
    }

    @Test
    void createDepartment_withParent_addsManagerToParentAndSavesBoth() {
        Department parent = Department.builder().id(2L).name("Parent").members(new HashSet<>()).build();
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", 2L, OWNER_ID.toString());
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(manager));
        when(userRepository.existsUserAsManager(OWNER_ID)).thenReturn(false);
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(userRepository.existsUserInOtherDepartmentsAsMember(OWNER_ID)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(departmentMapper.toDto(any(Department.class))).thenReturn(new DepartmentDTO(
                5L, "Development", "Description", 2L));

        departmentService.createDepartment(request);

        assertThat(parent.getMembers()).contains(manager);
        assertThat(manager.getDepartment()).isSameAs(parent);
        verify(departmentRepository, org.mockito.Mockito.times(2)).save(any(Department.class));
    }

    @Test
    void createDepartment_withParent_doesNotAddManagerAlreadyInOtherDepartment() {
        Department parent = Department.builder().id(2L).name("Parent").members(new HashSet<>()).build();
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", 2L, OWNER_ID.toString());
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(manager));
        when(userRepository.existsUserAsManager(OWNER_ID)).thenReturn(false);
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(userRepository.existsUserInOtherDepartmentsAsMember(OWNER_ID)).thenReturn(true);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(departmentMapper.toDto(any(Department.class))).thenReturn(new DepartmentDTO(
                5L, "Development", "Description", 2L));

        departmentService.createDepartment(request);

        assertThat(parent.getMembers()).isEmpty();
        verify(departmentRepository, org.mockito.Mockito.times(1)).save(any(Department.class));
    }

    @Test
    void createDepartment_missingManager_throws() {
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", 0L, OWNER_ID.toString());
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Manager");
    }

    @Test
    void createDepartment_managerOfAnotherDepartment_throws() {
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", 0L, OWNER_ID.toString());
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(manager));
        when(userRepository.existsUserAsManager(OWNER_ID)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(ManagerOfAnotherDepartmentException.class);
    }

    @Test
    void createDepartment_missingParent_throws() {
        CreateDepartmentDTO request = new CreateDepartmentDTO(
                "Development", "Description", 2L, OWNER_ID.toString());
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(manager));
        when(userRepository.existsUserAsManager(OWNER_ID)).thenReturn(false);
        when(departmentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parent department");
    }

    @Test
    void getDepartmentsWithUser_mapsAllDepartments() {
        when(departmentRepository.getDepartmentsWithUser(OWNER_ID)).thenReturn(List.of(department));
        when(departmentMapper.toDto(department)).thenReturn(new DepartmentDTO(5L, "Development", "", 0L));

        assertThat(departmentService.getDepartmentsWithUser(OWNER_ID)).hasSize(1);
    }

    @Test
    void getManagerOfDepartment_returnsMappedManager() {
        UserDTO expected = new UserDTO(
                OWNER_ID, "manager", "manager@example.com", "First", "Last", null, "Engineer", Set.of());
        when(userRepository.getDepartmentManager(5L)).thenReturn(Optional.of(manager));
        when(userMapper.toDTO(manager)).thenReturn(expected);

        assertThat(departmentService.getManagerOfDepartment(5L)).isSameAs(expected);
    }

    @Test
    void getManagerOfDepartment_missingManager_throws() {
        when(userRepository.getDepartmentManager(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getManagerOfDepartment(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllMembersOfDepartment_mapsPage() {
        Page<User> page = new PageImpl<>(List.of(member));
        UserPageDTO expected = new UserPageDTO(0, 1, 1, 0, List.of());
        when(userRepository.getDepartmentMembers(5L, PageRequest.of(0, 10))).thenReturn(page);
        when(userMapper.toPageDTO(page)).thenReturn(expected);

        assertThat(departmentService.getAllMembersOfDepartment(0, 10, 5L)).isSameAs(expected);
    }

    @Test
    void getAllSubordinateDepartments_mapsPage() {
        Page<Department> page = new PageImpl<>(List.of(department));
        DepartmentPageDTO expected = new DepartmentPageDTO(0, 10, 1, 1, Set.of());
        when(departmentRepository.getSubordinateDepartments(OWNER_ID, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(departmentMapper.toPageDTO(page)).thenReturn(expected);

        assertThat(departmentService.getAllSubordinateDepartments(0, 10, OWNER_ID)).isSameAs(expected);
    }

    @Test
    void addMembersToDepartment_addsAvailableAndSkipsMissingAndOccupiedUsers() {
        User occupied = user(THIRD_ID, "occupied");
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(member));
        when(userRepository.findById(THIRD_ID)).thenReturn(Optional.of(occupied));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());
        when(userRepository.existsUserInOtherDepartmentsAsMember(SIGNER_ID)).thenReturn(false);
        when(userRepository.existsUserInOtherDepartmentsAsMember(THIRD_ID)).thenReturn(true);

        StatisticMembersDTO result = departmentService.addMembersToDepartment(
                new MembersDTO(List.of(SIGNER_ID, THIRD_ID, OWNER_ID)), 5L);

        assertThat(result.success()).containsExactly(SIGNER_ID);
        assertThat(result.skipped()).containsExactlyInAnyOrder(THIRD_ID, OWNER_ID);
        assertThat(member.getDepartment()).isSameAs(department);
        assertThat(department.getMembers()).contains(member);
        verify(userRepository).save(member);
        verify(departmentRepository).save(department);
    }

    @Test
    void addMembersToDepartment_missingDepartment_throws() {
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.addMembersToDepartment(
                new MembersDTO(List.of(SIGNER_ID)), 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void kickMembersFromDepartment_kicksMembersAndSkipsOthers() {
        User nonMember = user(THIRD_ID, "non-member");
        department.getMembers().add(member);
        member.setDepartment(department);
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(member));
        when(userRepository.findById(THIRD_ID)).thenReturn(Optional.of(nonMember));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        StatisticMembersDTO result = departmentService.kickMembersFromDepartment(
                new MembersDTO(List.of(SIGNER_ID, THIRD_ID, OWNER_ID)), 5L);

        assertThat(result.success()).containsExactly(SIGNER_ID);
        assertThat(result.skipped()).containsExactlyInAnyOrder(THIRD_ID, OWNER_ID);
        assertThat(member.getDepartment()).isNull();
        assertThat(department.getMembers()).doesNotContain(member);
        verify(userRepository).save(member);
    }

    @Test
    void kickMembersFromDepartment_missingDepartment_throws() {
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.kickMembersFromDepartment(
                new MembersDTO(List.of(SIGNER_ID)), 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void leaveFromDepartment_managerLeaves_clearsManagerAndLeadDepartment() {
        member.setDepartment(department);
        member.setLeadDepartment(department);
        department.setManager(member);
        department.getMembers().add(member);
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(member));
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));

        departmentService.leaveFromDepartment(SIGNER_ID, 5L);

        assertThat(department.getManager()).isNull();
        assertThat(member.getDepartment()).isNull();
        assertThat(member.getLeadDepartment()).isNull();
        assertThat(department.getMembers()).doesNotContain(member);
        verify(departmentRepository).save(department);
        verify(userRepository).save(member);
    }

    @Test
    void leaveFromDepartment_regularMemberLeaves() {
        User otherManager = user(THIRD_ID, "other");
        department.setManager(otherManager);
        department.getMembers().add(member);
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(member));
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));

        departmentService.leaveFromDepartment(SIGNER_ID, 5L);

        assertThat(department.getManager()).isSameAs(otherManager);
        assertThat(member.getDepartment()).isNull();
    }

    @Test
    void leaveFromDepartment_missingUser_throws() {
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.leaveFromDepartment(SIGNER_ID, 5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void leaveFromDepartment_missingDepartment_throws() {
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(member));
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.leaveFromDepartment(SIGNER_ID, 5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department");
    }

    @Test
    void deleteDepartment_clearsManagerAndAllMemberReferences() {
        member.setDepartment(department);
        member.setLeadDepartment(department);
        User otherMember = user(THIRD_ID, "other");
        Department otherDepartment = Department.builder().id(6L).name("Other").build();
        otherMember.setDepartment(otherDepartment);
        otherMember.setLeadDepartment(otherDepartment);
        department.setManager(manager);
        manager.setLeadDepartment(department);
        department.getMembers().addAll(Set.of(member, otherMember));
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));

        departmentService.deleteDepartment(5L);

        assertThat(department.getManager()).isNull();
        assertThat(manager.getLeadDepartment()).isNull();
        assertThat(member.getDepartment()).isNull();
        assertThat(member.getLeadDepartment()).isNull();
        assertThat(otherMember.getDepartment()).isSameAs(otherDepartment);
        assertThat(department.getMembers()).isEmpty();
        verify(userRepository).save(manager);
        verify(userRepository).save(member);
        verify(userRepository, never()).save(otherMember);
        verify(departmentRepository, org.mockito.Mockito.times(2)).flush();
        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartment_withoutManager_stillDeletes() {
        department.getMembers().add(member);
        member.setDepartment(department);
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));

        departmentService.deleteDepartment(5L);

        assertThat(member.getDepartment()).isNull();
        verify(userRepository, never()).save(manager);
        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartment_missingDepartment_throws() {
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
