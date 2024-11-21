package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.ReplacementManagementService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplacementManagementServiceImpl implements ReplacementManagementService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public List<User> getAllUsersForReplacement(UUID userId) {
        Optional<Long> departmentByManagerUuid = departmentRepository.findDepartmentByManagerUuid(userId);
        Optional<Long> departmentByMemberUuid = departmentRepository.findDepartmentByMemberUuid(userId);
        if (!departmentByManagerUuid.isEmpty()) {
            List<User> departmentMembersForReplacementManager = userRepository.getDepartmentMembersForReplacementManager(departmentByManagerUuid.get());
            return departmentMembersForReplacementManager;
        } else {
            List<User> departmentManagersForReplacementDepartmentMember = userRepository.getDepartmentManagersForReplacementDepartmentMember(departmentByMemberUuid.get());
            List<User> departmentMembersForReplacement = userRepository.getDepartmentMembersForReplacement(userId, departmentByMemberUuid.get());
            departmentMembersForReplacement.addAll(departmentManagersForReplacementDepartmentMember);
            return departmentMembersForReplacement;
        }
    }

    public Page<User> getAllUsersForReplacement(int page, int size, UUID userId) {
        List<User> users = getAllUsersForReplacement(userId);
        PageRequest pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());

        users.subList(start, end);

        return new PageImpl<>(users, pageable, users.size());
    }
}