package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.mapper.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.mapper.ApprovementProcessMapper;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.service.ApprovementService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.DocumentVersionService;

import java.util.List;

import static ru.caselab.edm.backend.enums.ApprovementProcessStatus.INPROCESS;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovementServiceImpl implements ApprovementService {
    private final DocumentService documentService;
    private final ApprovementProcessRepository processRepository;
    private final DocumentVersionService documentVersionService;
    private final ApprovementProccessItemMapper itemMapper;
    private final ApprovementProcessMapper proccessMapper;

    @Override
    public ApprovementProcessDTO createApprovementProcess(ApprovementProcessCreateDTO createProcess, UserInfoDetails authenticatedUser) {
        log.info("Started approval process for document version {}",createProcess.getDocumentVersionId());
        Long documentVersionId = createProcess.getDocumentVersionId();
        ApprovementProcess process = new ApprovementProcess();
        process.setAgreementProcent(createProcess.getAgreementPercent());
        process.setStatus(INPROCESS);
        process.setDocumentVersion(documentVersionService.getDocumentVersion(documentVersionId));
        process.setDeadline(createProcess.getDeadline());
        List<ApprovementProcessItemDTO> items = createProcess.getUsersIds().stream().map(u->documentService.sendForSign(u,documentVersionId,authenticatedUser)).toList();
        process.setApprovementProcessItems(itemMapper.toEntityList(items));
        return proccessMapper.toDTO(processRepository.save(process));
    }
}
