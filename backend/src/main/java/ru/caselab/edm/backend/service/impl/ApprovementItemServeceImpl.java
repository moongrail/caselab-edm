package ru.caselab.edm.backend.service.impl;

import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.service.ApprovementItemService;

import java.util.UUID;

public class ApprovementItemServeceImpl implements ApprovementItemService {
    @Override
    public ApprovementProcessItem createItemForAprovementProcess(UUID UserId, Long documentVersionId, Long processId) {
        return null;
    }
}
