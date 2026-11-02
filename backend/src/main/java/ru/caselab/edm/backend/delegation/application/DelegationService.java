package ru.caselab.edm.backend.delegation.application;

import ru.caselab.edm.backend.shared.directory.api.UserInfoPageDTO;

import java.util.UUID;

public interface DelegationService {
    UserInfoPageDTO getAvailableUsersForDelegation(UUID userId, Long documentId, int size, int page);

    void delegateSign(UUID userIdToOelegate, UUID userIdFromDelegate, Long documentId);
}
