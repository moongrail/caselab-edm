package ru.caselab.edm.backend.service.impl;

import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.service.ApprovementItemService;

import java.util.UUID;

@Service
public class ApprovementItemServiceImpl implements ApprovementItemService {
    @Override
    public ApprovementProcessItem createItemForAprovementProcess(UUID UserId, Long documentVersionId, Long processId) {
        return null;
    }
}
