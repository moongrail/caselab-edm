package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface DelegationService {
    UserPageDTO getAvailableUsersForDelegation(UUID userId, Long documentId, int size, int page);
    void delegateSign(UUID userIdToOelegate,UUID userIdFromDelegate, Long documentId);
}
