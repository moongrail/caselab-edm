package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.replacementmanagement.UsersForReplacementDTO;

import java.util.UUID;

public interface ReplacementManagementService {
    Page<UsersForReplacementDTO> getAllUsersForReplacementManager(int page, int size, UUID userId);

    Page<UsersForReplacementDTO> getAllUsersForReplacementDepartmentMember(int page, int size, UUID userId);
}
