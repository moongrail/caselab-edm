package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.approvementproccess.ApprovementProcessMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.repository.ReplacementManagerRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.impl.ApprovementServiceImpl;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.THIRD_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class ApprovementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReplacementManagerRepository replacementManagerRepository;

    @Mock
    private ApprovementProcessRepository processRepository;

    @Mock
    private ApprovementItemRepository itemRepository;

    @Mock
    private ApprovementProcessMapper processMapper;

    @Mock
    private VotingService votingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentVersionService documentVersionService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private UserInfoDetails authenticatedUser;

    @InjectMocks
    private ApprovementServiceImpl approvementService;

    private User owner;
    private User signer;
    private Document document;
    private DocumentVersion documentVersion;

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID, "owner");
        signer = user(SIGNER_ID, "signer");
        document = document(DOCUMENT_ID, owner, documentType(1L));
        documentVersion = version(VERSION_ID, document, DocumentStatus.DRAFT);
        lenient().when(authenticatedUser.getId()).thenReturn(OWNER_ID);
        lenient().when(documentService.getLastVersionDocumentForUser(DOCUMENT_ID, OWNER_ID))
                .thenReturn(documentVersion);
    }

    @Test
    void createApprovementProcess_createsItemsAndSchedulesVoting() {
        ApprovementProcessCreateDTO request = request(Set.of(SIGNER_ID), 60f);
        ApprovementProcessDTO expected = new ApprovementProcessDTO();
        when(replacementManagerRepository.findActiveReplacementsByManagerUserIds(anySet())).thenReturn(List.of());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(signer));
        when(processRepository.save(any(ApprovementProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(any(ApprovementProcessItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processMapper.toDTO(any(ApprovementProcess.class))).thenReturn(expected);

        ApprovementProcessDTO result = approvementService.createApprovementProcess(request, authenticatedUser);

        assertThat(result).isSameAs(expected);
        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.IN_VOTING);
        assertThat(documentVersion.getApprovementProcesses()).hasSize(1);

        ArgumentCaptor<ApprovementProcess> processCaptor = ArgumentCaptor.forClass(ApprovementProcess.class);
        verify(processRepository, org.mockito.Mockito.atLeastOnce()).save(processCaptor.capture());
        ApprovementProcess process = processCaptor.getValue();
        assertThat(process.getStatus()).isEqualTo(ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        assertThat(process.getDeadline()).isEqualTo(request.getDeadline());
        assertThat(process.getAgreementProcent()).isEqualTo(60f);
        assertThat(process.getApprovementProcessItems()).hasSize(1);
        assertThat(process.getApprovementProcessItems().get(0).getUser()).isSameAs(signer);
        assertThat(process.getApprovementProcessItems().get(0).getStatus())
                .isEqualTo(ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);

        verify(votingService).scheduleVotingJob(process.getId(), request.getDeadline());
        verify(eventPublisher).publishEvent(any(DocumentSignRequestEvent.class));
    }

    @Test
    void createApprovementProcess_usesReplacementInsteadOfManager() {
        ApprovementProcessCreateDTO request = request(Set.of(SIGNER_ID), 50f);
        User tempManager = user(THIRD_ID, "temp");
        ReplacementManager replacement = ReplacementManager.builder()
                .id(1L)
                .managerUser(signer)
                .tempManagerUser(tempManager)
                .startDate(Instant.now().minusSeconds(60))
                .endDate(Instant.now().plusSeconds(60))
                .build();
        when(replacementManagerRepository.findActiveReplacementsByManagerUserIds(anySet()))
                .thenReturn(List.of(replacement));
        when(processRepository.save(any(ApprovementProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(any(ApprovementProcessItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processMapper.toDTO(any(ApprovementProcess.class))).thenReturn(new ApprovementProcessDTO());

        approvementService.createApprovementProcess(request, authenticatedUser);

        verify(userRepository, never()).findById(SIGNER_ID);
        ArgumentCaptor<ApprovementProcessItem> itemCaptor =
                ArgumentCaptor.forClass(ApprovementProcessItem.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getUser()).isSameAs(tempManager);
    }

    @Test
    void createApprovementProcess_initializesProcessListIfNull() {
        documentVersion.setApprovementProcesses(null);
        ApprovementProcessCreateDTO request = request(Set.of(SIGNER_ID), 50f);
        when(replacementManagerRepository.findActiveReplacementsByManagerUserIds(anySet())).thenReturn(List.of());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(signer));
        when(processRepository.save(any(ApprovementProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(any(ApprovementProcessItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processMapper.toDTO(any(ApprovementProcess.class))).thenReturn(new ApprovementProcessDTO());

        approvementService.createApprovementProcess(request, authenticatedUser);

        assertThat(documentVersion.getApprovementProcesses()).hasSize(1);
    }

    @Test
    void createApprovementProcess_withoutParticipants_createsEmptyProcess() {
        ApprovementProcessCreateDTO request = request(Set.of(), 50f);
        when(replacementManagerRepository.findActiveReplacementsByManagerUserIds(anySet())).thenReturn(List.of());
        when(processRepository.save(any(ApprovementProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processMapper.toDTO(any(ApprovementProcess.class))).thenReturn(new ApprovementProcessDTO());

        approvementService.createApprovementProcess(request, authenticatedUser);

        verify(itemRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(votingService).scheduleVotingJob(any(), any());
    }

    @Test
    void createApprovementProcess_forbiddenForNonOwner() {
        when(authenticatedUser.getId()).thenReturn(SIGNER_ID);
        when(documentService.getLastVersionDocumentForUser(DOCUMENT_ID, SIGNER_ID))
                .thenReturn(documentVersion);
        org.mockito.Mockito.doThrow(new DocumentForbiddenAccess("forbidden"))
                .when(authorizationService).checkDocumentOwner(document, SIGNER_ID);
        ApprovementProcessCreateDTO request = request(Set.of(SIGNER_ID), 50f);

        assertThatThrownBy(() -> approvementService.createApprovementProcess(request, authenticatedUser))
                .isInstanceOf(DocumentForbiddenAccess.class);

        verify(authorizationService).checkDocumentOwner(document, SIGNER_ID);
    }

    @Test
    void createApprovementProcess_missingParticipant_throws() {
        ApprovementProcessCreateDTO request = request(Set.of(SIGNER_ID), 50f);
        when(replacementManagerRepository.findActiveReplacementsByManagerUserIds(anySet())).thenReturn(List.of());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.empty());
        when(processRepository.save(any(ApprovementProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> approvementService.createApprovementProcess(request, authenticatedUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(SIGNER_ID.toString());
    }

    @Test
    void resultOfApprovementProcess_returnsMappedResult() {
        ApprovementProcess process = new ApprovementProcess();
        ApprovementProcessResultDTO expected = new ApprovementProcessResultDTO();
        when(documentVersionService.getDocumentVersion(DOCUMENT_ID)).thenReturn(documentVersion);
        when(processRepository.getApprovementProcessByDocumentVersion(documentVersion))
                .thenReturn(Optional.of(process));
        when(processMapper.toResultDTO(process)).thenReturn(expected);

        ApprovementProcessResultDTO result = approvementService.resultOfApprovementProcess(DOCUMENT_ID);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void resultOfApprovementProcess_missingProcess_throws() {
        when(documentVersionService.getDocumentVersion(DOCUMENT_ID)).thenReturn(documentVersion);
        when(processRepository.getApprovementProcessByDocumentVersion(documentVersion))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvementService.resultOfApprovementProcess(DOCUMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approvement process not found");
    }

    private ApprovementProcessCreateDTO request(Set<UUID> users, float agreementPercent) {
        return new ApprovementProcessCreateDTO(
                DOCUMENT_ID,
                LocalDateTime.now().plusDays(2),
                agreementPercent,
                users
        );
    }
}
