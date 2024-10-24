package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;

public interface ApprovementService {

    ApprovementProcessDTO createApprovementProcess(ApprovementProcessCreateDTO createProcess);
}
