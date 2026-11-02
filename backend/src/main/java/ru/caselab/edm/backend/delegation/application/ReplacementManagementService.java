package ru.caselab.edm.backend.delegation.application;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.delegation.api.dto.replacementmanagement.CreateReplacementDTO;
import ru.caselab.edm.backend.delegation.domain.ReplacementManager;
import ru.caselab.edm.backend.shared.directory.UserSummary;

import java.util.List;
import java.util.UUID;

public interface ReplacementManagementService {
    Page<UserSummary> getAllUsersForReplacement(int page, int size, UUID userId);

    List<UserSummary> getAllUsersForReplacement(UUID userId);

    ReplacementManager createReplacement(CreateReplacementDTO replacementData, UUID userId);
}
