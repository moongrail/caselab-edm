package ru.caselab.edm.security.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.security.domain.Department;
import ru.caselab.edm.security.domain.Role;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.exception.ResourceNotFoundException;
import ru.caselab.edm.security.persistence.DepartmentRepository;
import ru.caselab.edm.security.persistence.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only directory facade for internal service-to-service calls.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalUserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public InternalUserDTO get(UUID id) {
        return toDto(requireUser(id));
    }

    public List<InternalUserDTO> getByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, User> users = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        return ids.stream()
                .distinct()
                .map(users::get)
                .filter(java.util.Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    public List<InternalUserDTO> replacementCandidates(UUID userId) {
        User user = requireUser(userId);
        Map<UUID, User> candidates = new LinkedHashMap<>();

        departmentRepository.findDepartmentByManagerUuid(userId).ifPresent(departmentId -> {
            departmentRepository.findById(departmentId).ifPresent(department -> {
                if (department.getMembers() != null) {
                    department.getMembers().forEach(member -> candidates.put(member.getId(), member));
                }
            });
            candidates.putAll(userRepository.getDepartmentMembersForReplacementManager(departmentId)
                    .stream().collect(Collectors.toMap(User::getId, member -> member)));
        });

        departmentRepository.findDepartmentByMemberUuid(userId).ifPresent(departmentId -> {
            userRepository.getDepartmentManagersForReplacementDepartmentMember(departmentId)
                    .forEach(manager -> candidates.put(manager.getId(), manager));
            userRepository.getDepartmentMembersForReplacement(userId, departmentId)
                    .forEach(member -> candidates.put(member.getId(), member));
        });

        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException(
                    "User with id %s is not a member of any department".formatted(userId));
        }
        candidates.remove(userId);
        return new ArrayList<>(candidates.values()).stream().map(this::toDto).toList();
    }

    public InternalUserPageDTO departmentMembers(Long departmentId,
                                                 Collection<UUID> exclude,
                                                 int page,
                                                 int size) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with ID = %s not found".formatted(departmentId)));
        PageRequest pageable = PageRequest.of(page, size);
        List<UUID> excluded = exclude == null ? List.of() : List.copyOf(exclude);
        Page<User> members = userRepository.getAvailableUsersForDelegation(
                department.getId(), excluded.isEmpty() ? List.of(NO_EXCLUSION) : excluded, pageable);
        return new InternalUserPageDTO(
                members.getContent().stream().map(this::toDto).toList(),
                members.getNumber(),
                members.getSize(),
                members.getTotalElements(),
                members.getTotalPages()
        );
    }

    public InternalUserDTO departmentManager(Long departmentId) {
        return userRepository.getDepartmentManager(departmentId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department manager not found for department %s".formatted(departmentId)));
    }

    private User requireUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID = %s not found".formatted(id)));
    }

    private InternalUserDTO toDto(User user) {
        Set<String> roles = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream().map(Role::getName).map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        Department department = user.getDepartment();
        return new InternalUserDTO(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPatronymic(),
                user.getPosition(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName(),
                user.getLeadDepartment() == null ? null : user.getLeadDepartment().getId(),
                roles
        );
    }

    private static final UUID NO_EXCLUSION =
            UUID.fromString("00000000-0000-0000-0000-000000000000");
}
