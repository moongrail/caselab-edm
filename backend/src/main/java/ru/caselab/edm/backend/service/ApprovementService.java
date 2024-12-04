package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;

public interface ApprovementService {

    ApprovementProcessDTO createApprovementProcess(ApprovementProcessCreateDTO createProcess, UserInfoDetails authenticatedUser);
    ApprovementProcessResultDTO resultOfApprovementProcess(Long documentId);
}
