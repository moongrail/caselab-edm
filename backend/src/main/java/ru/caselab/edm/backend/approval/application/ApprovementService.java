package ru.caselab.edm.backend.approval.application;

import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.platform.security.AuthenticatedUser;

public interface ApprovementService {

    ApprovementProcessDTO createApprovementProcess(ApprovementProcessCreateDTO createProcess, AuthenticatedUser authenticatedUser);
    ApprovementProcessResultDTO resultOfApprovementProcess(Long documentId);
}
