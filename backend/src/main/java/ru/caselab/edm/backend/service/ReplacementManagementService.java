package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface ReplacementManagementService {
    Page<User> getAllUsersForReplacement(int page, int size, UUID userId);

    List<User> getAllUsersForReplacement(UUID userId);
}
