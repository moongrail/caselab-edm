package ru.caselab.edm.backend.delegation.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.delegation.api.dto.replacementmanagement.CreateReplacementDTO;
import ru.caselab.edm.backend.delegation.application.ReplacementManagementService;
import ru.caselab.edm.backend.delegation.domain.ReplacementManager;
import ru.caselab.edm.backend.delegation.exception.NotDepartmentMemberException;
import ru.caselab.edm.backend.delegation.exception.WrongDateException;
import ru.caselab.edm.backend.delegation.infrastructure.ReplacementManagerRepository;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplacementManagementServiceImpl implements ReplacementManagementService {

    private final UserDirectory userDirectory;
    private final ReplacementManagerRepository replacementManagerRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserSummary> getAllUsersForReplacement(UUID userId) {
        List<UserSummary> users = userDirectory.replacementCandidates(userId);
        if (users.isEmpty()) {
            throw new NotDepartmentMemberException(
                    "User with id %s is not a member of any department".formatted(userId));
        }
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserSummary> getAllUsersForReplacement(int page, int size, UUID userId) {
        List<UserSummary> users = getAllUsersForReplacement(userId);
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

        UserSummary authenticatedUser = userDirectory.requireUser(userId);
        UserSummary replacementUser = userDirectory.requireUser(replacementData.userId());

        boolean available = getAllUsersForReplacement(userId).stream()
                .anyMatch(user -> user.id().equals(replacementUser.id()));
        if (!available) {
            throw new NotDepartmentMemberException(
                    "User with ID %s unavailable for replacement".formatted(replacementData.userId()));
        }

        ReplacementManager replacement = new ReplacementManager();
        replacement.setManagerUserId(authenticatedUser.id());
        replacement.setTempManagerUserId(replacementUser.id());
        replacement.setStartDate(replacementData.start());
        replacement.setEndDate(replacementData.end());
        log.info("Replacement created: manager {}, deputy {}", userId, replacementUser.id());
        return replacementManagerRepository.save(replacement);
    }
}
