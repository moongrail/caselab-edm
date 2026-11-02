package ru.caselab.edm.backend.document.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import ru.caselab.edm.backend.approval.api.ApprovalController;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.document.api.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.document.api.dto.file.FileDTO;
import ru.caselab.edm.backend.approval.api.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.platform.security.AuthenticatedUser;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessStatus;
import ru.caselab.edm.backend.document.domain.DocumentSortingType;
import ru.caselab.edm.backend.document.api.mapper.DocumentVersionMapper;
import ru.caselab.edm.backend.approval.application.ApprovementService;
import ru.caselab.edm.backend.document.application.AuthorizationService;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.backend.approval.application.SignatureService;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;
import ru.caselab.edm.backend.shared.testutils.base.BaseControllerTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@ActiveProfiles("test")
@WebMvcTest({DocumentController.class, ApprovalController.class})
public class DocumentControllerTest extends BaseControllerTest {

    private static final String BASE_URI = "/document";
    private static final Long ID = 1L;
    private static final int INVALID_LENGTH = 256;

    @MockBean
    private DocumentService documentService;
    @MockBean
    private AuthorizationService authorizationService;
    @MockBean
    private ApprovementService approvementService;
    @MockBean
    private SignatureService signatureService;
    @MockBean
    private ObjectStorage objectStorage;
    @MockBean
    private DocumentVersionMapper documentVersionMapper;
    private AuthenticatedUser user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userId = UUID.randomUUID();
        user = new AuthenticatedUser(userId, "user", Set.of("USER"), null, null);
    }

    @Test
    void downloadDocument_authorized_returnsTemporaryLink() throws Exception {

        when(objectStorage.temporaryUrl("content/1"))
                .thenReturn("http://minio/download");

        performRequest(get(BASE_URI + "/download").param("url", "content/1"), null, user)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.link").value("http://minio/download"));

        verify(authorizationService).ensureContentAccess("content/1", userId);
        verify(objectStorage).temporaryUrl("content/1");
    }

    @Test
    void downloadDocument_forbidden_doesNotGenerateLink() throws Exception {

        org.mockito.Mockito.doThrow(new ru.caselab.edm.backend.document.exception.DocumentForbiddenAccess("forbidden"))
                .when(authorizationService).ensureContentAccess("content/1", userId);

        performRequest(get(BASE_URI + "/download").param("url", "content/1"), null, user)
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(objectStorage, never()).temporaryUrl(any());
    }

    @Test
    void getAllDocumentsForUser_returnsPage() throws Exception {
        DocumentOutputAllDocumentsDTO document = new DocumentOutputAllDocumentsDTO(
                ID,
                "author",
                Instant.parse("2026-01-01T00:00:00Z"),
                "document",
                "content/1",
                DocumentStatus.DRAFT
        );

        when(documentService.getAllDocumentForUser(0, 10, userId, DocumentSortingType.WITHOUT))
                .thenReturn(new PageImpl<>(List.of(document)));

        performRequest(get(BASE_URI + "/owner"), null, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ID))
                .andExpect(jsonPath("$.content[0].documentName").value("document"));

        verify(documentService).getAllDocumentForUser(0, 10, userId, DocumentSortingType.WITHOUT);
    }

    @Test
    void getArchivedDocuments_returnsPage() throws Exception {

        when(documentService.getArchivedDocuments(0, 10, userId))
                .thenReturn(new PageImpl<>(List.of()));

        performRequest(get(BASE_URI + "/archive"), null, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(documentService).getArchivedDocuments(0, 10, userId);
    }

    @Test
    void sendForSignature_returnsCreatedItem() throws Exception {
        ApprovementProcessItemDTO item = new ApprovementProcessItemDTO();
        item.setId(ID);
        item.setUserId(userId);
        item.setStatus(ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        when(documentService.sendForSign(userId, ID, user)).thenReturn(item);

        performRequest(post(BASE_URI + "/{id}/send_for_signature", ID)
                        .param("userId", userId.toString()), null, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.status").value("PENDING_AUTHOR_SIGN"));

        verify(documentService).sendForSign(userId, ID, user);
    }

    @Test
    void signDocument_delegatesToSignatureService() throws Exception {
        SignatureCreateDTO signature = new SignatureCreateDTO();
        signature.setStatus("APPROVED");

        performRequest(post(BASE_URI + "/{id}/sign", ID), signature, user)
                .andExpect(status().isOk());

        verify(signatureService).sign(any(SignatureCreateDTO.class), eq(ID), eq(user));
    }

    @Test
    void resultApprovement_returnsResult() throws Exception {
        ApprovementProcessResultDTO result = new ApprovementProcessResultDTO();
        result.setDocumentId(ID);
        result.setStatus(ApprovementProcessStatus.VOTING_APPROVED);
        when(approvementService.resultOfApprovementProcess(ID)).thenReturn(result);

        performRequest(post(BASE_URI + "/approvement/{id}/result", ID), null, user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(ID))
                .andExpect(jsonPath("$.status").value("VOTING_APPROVED"));

        verify(approvementService).resultOfApprovementProcess(ID);
    }

    @Test
    void deleteDocument_delegatesToService() throws Exception {


        performRequest(delete(BASE_URI + "/{id}", ID), null, user)
                .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(ID, userId);
    }

    @Test
    void createDocument_validDto_shouldCreateDocumentWithStatusOk() throws Exception {
        DocumentCreateDTO createDto = new DocumentCreateDTO(
                ID,
                "documentName",
                new FileDTO("dasdaxzcdgs", "doc.pdf"),
                null
        );



        performRequest(post(BASE_URI), createDto, user)
                .andDo(print())
                .andExpect(status().isCreated());


        verify(documentService).saveDocument(any(DocumentCreateDTO.class), eq(userId));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void createDocument_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        DocumentCreateDTO createDto = new DocumentCreateDTO(
                ID,
                name,
                new FileDTO("dasdaxzcdgs", "doc.pdf"),
                null
        );

        performRequest(post(BASE_URI), createDto, user)
                .andDo(print())
                .andExpect(status().isBadRequest());



        verify(documentService, never()).saveDocument(any(DocumentCreateDTO.class), eq(userId));
    }

    @Test
    void updateDocument_validDto_shouldUpdateWithStatusOk() throws Exception {
        DocumentUpdateDTO updateDto = new DocumentUpdateDTO(
                "name",
                new FileDTO("dasdaxzcdgs", "doc.pdf"),
                null
        );



        performRequest(put(BASE_URI + "/{id}", ID), updateDto, user)
                .andDo(print())
                .andExpect(status().isOk());


        verify(documentService).updateDocument(eq(ID), any(DocumentUpdateDTO.class), eq(userId));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void updateDocument_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        DocumentUpdateDTO updateDto = new DocumentUpdateDTO(
                name,
                new FileDTO("dasdaxzcdgs", "doc.pdf"),
                null
        );

        performRequest(put(BASE_URI + "/{id}", ID), updateDto, user)
                .andDo(print())
                .andExpect(status().isBadRequest());


        verify(documentService, never()).updateDocument(eq(ID), any(DocumentUpdateDTO.class), eq(userId));

    }

    @Test
    void startApprovement_validDto_shouldStartWithStatusOk() throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                ID,
                LocalDateTime.now().plusDays(10),
                10,
                Set.of(userId)
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, user)
                .andDo(print())
                .andExpect(status().isOk());

        verify(approvementService).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(user));
    }

    @MethodSource("getAgreementPercentValidationCases")
    @ParameterizedTest
    void startApprovement_invalidAgreementPercent_shouldReturnStatusBadRequest(float agreementPercent) throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                ID,
                LocalDateTime.now().plusDays(10),
                agreementPercent,
                Set.of(userId)
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, user)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(user));
    }

    @NullSource
    @ParameterizedTest
    void startApprovement_documentIdIsNull_shouldReturnStatusBadRequest(Long documentId) throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                documentId,
                LocalDateTime.now().plusDays(10),
                10,
                Set.of(userId)
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, user)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(user));
    }

    @Test
    void startApprovement_userIdsIsEmpty_shouldReturnStatusBadRequest() throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                ID,
                LocalDateTime.now().plusDays(10),
                10,
                new HashSet<>()
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, user)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(user));
    }


    @Test
    void startApprovement_deadlineIsInThePast_shouldReturnStatusBadRequest() throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                ID,
                LocalDateTime.now().minusDays(10),
                10,
                new HashSet<>()
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, user)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(user));
    }

    private static Stream<Arguments> getNameValidationCases() {
        return Stream.of(
                arguments(named("Name is null", null)),
                arguments(named("Name is blank", null)),
                arguments(named("Name is too short", "t")),
                arguments(named("Name is too long", generateStringWithInvalidLength()))
        );
    }

    private static Stream<Arguments> getAgreementPercentValidationCases() {
        return Stream.of(
                arguments(named("Percent less than min value", 0)),
                arguments(named("Percent greater than max value", 101))
        );
    }

    private static String generateStringWithInvalidLength() {
        return "x".repeat(INVALID_LENGTH);
    }
}
