package ru.caselab.edm.security.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.security.domain.Department;
import ru.caselab.edm.security.domain.Role;
import ru.caselab.edm.security.domain.RoleName;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.exception.ResourceNotFoundException;
import ru.caselab.edm.security.persistence.DepartmentRepository;
import ru.caselab.edm.security.persistence.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.security.testutils.UserFixtures.OWNER_ID;
import static ru.caselab.edm.security.testutils.UserFixtures.SIGNER_ID;
import static ru.caselab.edm.security.testutils.UserFixtures.user;

@ExtendWith(MockitoExtension.class)
class InternalUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    private InternalUserService service;
    private User user;
    private Department department;

    @BeforeEach
    void setUp() {
        service = new InternalUserService(userRepository, departmentRepository);
        department = Department.builder().id(5L).name("Development").build();
        user = user(OWNER_ID, "owner");
        user.setDepartment(department);
        user.setLeadDepartment(department);
        user.setRoles(Set.of(Role.builder().id(1L).name(RoleName.USER).build()));
    }

    @Test
    void get_returnsProjectionWithoutCredentials() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));

        InternalUserDTO dto = service.get(OWNER_ID);

        assertThat(dto.id()).isEqualTo(OWNER_ID);
        assertThat(dto.departmentName()).isEqualTo("Development");
        assertThat(dto.managedDepartmentId()).isEqualTo(5L);
        assertThat(dto.roles()).containsExactly("USER");
    }

    @Test
    void get_missingUser_throws() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIds_returnsOnlyFoundUsersInRequestOrder() {
        User second = user(SIGNER_ID, "second");
        when(userRepository.findAllById(any()))
                .thenReturn(List.of(second, user));

        List<InternalUserDTO> result = service.getByIds(List.of(OWNER_ID, SIGNER_ID));

        assertThat(result).extracting(InternalUserDTO::id)
                .containsExactly(OWNER_ID, SIGNER_ID);
    }

    @Test
    void getByIds_empty_returnsEmptyList() {
        assertThat(service.getByIds(List.of())).isEmpty();
        assertThat(service.getByIds(null)).isEmpty();
    }

    @Test
    void replacementCandidates_managerGetsDepartmentMembers() {
        User member = user(SIGNER_ID, "member");
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID))
                .thenReturn(Optional.of(5L));
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        department.setMembers(Set.of(user, member));
        when(userRepository.getDepartmentMembersForReplacementManager(5L))
                .thenReturn(List.of(member));
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());

        List<InternalUserDTO> result = service.replacementCandidates(OWNER_ID);

        assertThat(result).extracting(InternalUserDTO::id).containsExactly(SIGNER_ID);
    }

    @Test
    void replacementCandidates_memberGetsManagersAndMembers() {
        User manager = user(SIGNER_ID, "manager");
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.of(5L));
        when(userRepository.getDepartmentManagersForReplacementDepartmentMember(5L))
                .thenReturn(List.of(manager));
        when(userRepository.getDepartmentMembersForReplacement(OWNER_ID, 5L)).thenReturn(List.of());

        List<InternalUserDTO> result = service.replacementCandidates(OWNER_ID);

        assertThat(result).extracting(InternalUserDTO::id).containsExactly(SIGNER_ID);
    }

    @Test
    void replacementCandidates_noDepartments_throws() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replacementCandidates(OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void departmentMembers_mapsPageAndExclusions() {
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(userRepository.getAvailableUsersForDelegation(eq(5L), anyList(),
                eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of(user)));

        InternalUserPageDTO result = service.departmentMembers(
                5L, List.of(SIGNER_ID), 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.page()).isZero();
    }

    @Test
    void departmentMembers_missingDepartment_throws() {
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.departmentMembers(5L, List.of(), 0, 10))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void departmentManager_returnsUser() {
        when(userRepository.getDepartmentManager(5L)).thenReturn(Optional.of(user));

        assertThat(service.departmentManager(5L).id()).isEqualTo(OWNER_ID);
    }

    @Test
    void departmentManager_missing_throws() {
        when(userRepository.getDepartmentManager(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.departmentManager(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
