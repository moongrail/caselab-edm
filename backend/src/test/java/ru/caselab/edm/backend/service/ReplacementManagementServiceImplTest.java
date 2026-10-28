package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.caselab.edm.backend.dto.replacementmanagement.CreateReplacementDTO;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.NotDepartmentMemberException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.ReplacementManagerRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.ReplacementManagementServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.THIRD_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;

@ExtendWith(MockitoExtension.class)
class ReplacementManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private ReplacementManagerRepository replacementManagerRepository;

    @InjectMocks
    private ReplacementManagementServiceImpl service;

    @Test
    void getAllUsersForReplacement_managerGetsDepartmentMembers() {
        User member = user(SIGNER_ID, "member");
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.of(5L));
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(userRepository.getDepartmentMembersForReplacementManager(5L)).thenReturn(List.of(member));

        assertThat(service.getAllUsersForReplacement(OWNER_ID)).containsExactly(member);
    }

    @Test
    void getAllUsersForReplacement_memberGetsManagersAndOtherMembersWithoutDuplicates() {
        User manager = user(SIGNER_ID, "manager");
        User otherMember = user(THIRD_ID, "other");
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.of(5L));
        when(userRepository.getDepartmentManagersForReplacementDepartmentMember(5L))
                .thenReturn(List.of(manager, otherMember));
        when(userRepository.getDepartmentMembersForReplacement(OWNER_ID, 5L))
                .thenReturn(List.of(otherMember));

        assertThat(service.getAllUsersForReplacement(OWNER_ID))
                .containsExactly(manager, otherMember);
    }

    @Test
    void getAllUsersForReplacement_noDepartments_throws() {
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllUsersForReplacement(OWNER_ID))
                .isInstanceOf(NotDepartmentMemberException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    void getAllUsersForReplacement_pageOutsideRange_returnsEmptyPageWithTotal() {
        User member = user(SIGNER_ID, "member");
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.of(5L));
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(userRepository.getDepartmentMembersForReplacementManager(5L)).thenReturn(List.of(member));

        Page<User> page = service.getAllUsersForReplacement(1, 10, OWNER_ID);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getPageable()).isEqualTo(PageRequest.of(1, 10));
    }

    @Test
    void getAllUsersForReplacement_pageInsideRange_returnsSubList() {
        User first = user(SIGNER_ID, "first");
        User second = user(THIRD_ID, "second");
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.of(5L));
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(userRepository.getDepartmentMembersForReplacementManager(5L))
                .thenReturn(List.of(first, second));

        Page<User> page = service.getAllUsersForReplacement(0, 1, OWNER_ID);

        assertThat(page.getContent()).containsExactly(first);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void createReplacement_success_savesReplacement() {
        User replacement = user(SIGNER_ID, "replacement");
        CreateReplacementDTO request = request(SIGNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user(OWNER_ID, "owner")));
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(replacement));
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.of(5L));
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(userRepository.getDepartmentMembersForReplacementManager(5L)).thenReturn(List.of(replacement));
        when(replacementManagerRepository.save(any(ReplacementManager.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReplacementManager result = service.createReplacement(request, OWNER_ID);

        assertThat(result.getManagerUser().getId()).isEqualTo(OWNER_ID);
        assertThat(result.getTempManagerUser()).isSameAs(replacement);
        assertThat(result.getStartDate()).isEqualTo(request.start());
        assertThat(result.getEndDate()).isEqualTo(request.end());
        verify(replacementManagerRepository).save(any(ReplacementManager.class));
    }

    @Test
    void createReplacement_startAfterEnd_throws() {
        CreateReplacementDTO request = new CreateReplacementDTO(
                SIGNER_ID, Instant.parse("2026-12-02T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z"));

        assertThatThrownBy(() -> service.createReplacement(request, OWNER_ID))
                .isInstanceOf(WrongDateException.class);
        verify(replacementManagerRepository, never()).save(any());
    }

    @Test
    void createReplacement_forSelf_throws() {
        CreateReplacementDTO request = request(OWNER_ID);

        assertThatThrownBy(() -> service.createReplacement(request, OWNER_ID))
                .isInstanceOf(NotDepartmentMemberException.class);
    }

    @Test
    void createReplacement_missingAuthenticatedUser_throws() {
        CreateReplacementDTO request = request(SIGNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReplacement(request, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(OWNER_ID.toString());
    }

    @Test
    void createReplacement_missingReplacementUser_throws() {
        CreateReplacementDTO request = request(SIGNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user(OWNER_ID, "owner")));
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReplacement(request, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(SIGNER_ID.toString());
    }

    @Test
    void createReplacement_unavailableUser_throws() {
        User replacement = user(SIGNER_ID, "replacement");
        CreateReplacementDTO request = request(SIGNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user(OWNER_ID, "owner")));
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(replacement));
        when(departmentRepository.findDepartmentByManagerUuid(OWNER_ID)).thenReturn(Optional.empty());
        when(departmentRepository.findDepartmentByMemberUuid(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReplacement(request, OWNER_ID))
                .isInstanceOf(NotDepartmentMemberException.class);
        verify(replacementManagerRepository, never()).save(any());
    }

    private CreateReplacementDTO request(java.util.UUID userId) {
        return new CreateReplacementDTO(
                userId,
                Instant.now().plusSeconds(60),
                Instant.now().plusSeconds(3600));
    }
}
