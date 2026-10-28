package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.replacementmanagement.CreateReplacementDTO;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.NotDepartmentMemberException;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.ReplacementManagerRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.ReplacementManagementService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplacementManagementServiceImpl implements ReplacementManagementService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ReplacementManagerRepository replacementManagerRepository;

    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsersForReplacement(UUID userId) {
        List<User> users = new ArrayList<>();

        departmentRepository.findDepartmentByManagerUuid(userId).ifPresent(departmentId ->
                users.addAll(userRepository.getDepartmentMembersForReplacementManager(departmentId)));

        departmentRepository.findDepartmentByMemberUuid(userId).ifPresent(departmentId -> {
            users.addAll(userRepository.getDepartmentManagersForReplacementDepartmentMember(departmentId));
            users.addAll(userRepository.getDepartmentMembersForReplacement(userId, departmentId));
        });

        if (users.isEmpty()) {
            throw new NotDepartmentMemberException(
                    "User with id %s is not a member of any department".formatted(userId));
        }

        Map<UUID, User> uniqueUsers = new LinkedHashMap<>();
        users.forEach(user -> uniqueUsers.putIfAbsent(user.getId(), user));
        return new ArrayList<>(uniqueUsers.values());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<User> getAllUsersForReplacement(int page, int size, UUID userId) {
        List<User> users = getAllUsersForReplacement(userId);
        PageRequest pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        if (start >= users.size()) {
            return new PageImpl<>(List.of(), pageable, users.size());
        }
        int end = Math.min(start + pageable.getPageSize(), users.size());
        return new PageImpl<>(new ArrayList<>(users.subList(start, end)), pageable, users.size());
    }

    @Transactional
    @Override
    public ReplacementManager createReplacement(CreateReplacementDTO replacementData, UUID userId) {
        if (replacementData.start().isAfter(replacementData.end())) {
            throw new WrongDateException("Дата начала замены не может быть позже даты окончания");
        }
        if (replacementData.userId().equals(userId)) {
            throw new NotDepartmentMemberException("Нельзя назначить замену самому себе");
        }

        User authenticatedUser = findUser(userId);
        User replacementUser = findUser(replacementData.userId());

        if (!getAllUsersForReplacement(userId).contains(replacementUser)) {
            throw new NotDepartmentMemberException(
                    "User with ID %s unavailable for replacement".formatted(replacementData.userId()));
        }

        ReplacementManager replacement = new ReplacementManager();
        replacement.setManagerUser(authenticatedUser);
        replacement.setTempManagerUser(replacementUser);
        replacement.setStartDate(replacementData.start());
        replacement.setEndDate(replacementData.end());
        log.info("Replacement created: manager {}, deputy {}", userId, replacementUser.getId());
        return replacementManagerRepository.save(replacement);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID = %s not found".formatted(id)));
    }
}
