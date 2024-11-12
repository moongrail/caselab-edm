package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.entity.User;

import java.util.UUID;

public interface ReplacementManagementService {
    Page<User> getUserForReplacement(int page, int size, UUID userId);
}
