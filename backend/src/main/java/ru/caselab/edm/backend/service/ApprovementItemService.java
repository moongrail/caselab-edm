package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.entity.ApprovementProcessItem;

import java.util.UUID;

public interface ApprovementItemService {

    ApprovementProcessItem createItemForAprovementProcess(UUID UserId, Long documentVersionId, Long processId);
}
