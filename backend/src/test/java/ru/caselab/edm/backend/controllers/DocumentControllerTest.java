package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.mapper.documentversion.DocumentVersionMapper;
import ru.caselab.edm.backend.service.ApprovementService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.MinioService;
import ru.caselab.edm.backend.service.SignatureService;
import ru.caselab.edm.backend.testutils.base.BaseControllerTest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(DocumentController.class)
public class DocumentControllerTest extends BaseControllerTest {

    private static final String BASE_URI = "/document";
    private static final Long ID = 1L;
    private static final int INVALID_LENGTH = 256;

    @MockBean
    private DocumentService documentService;
    @MockBean
    private ApprovementService approvementService;
    @MockBean
    private SignatureService signatureService;
    @MockBean
    private MinioService minioService;
    @MockBean
    private DocumentVersionMapper documentVersionMapper;
    @Mock
    private UserInfoDetails userInfoDetails;
    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userId = UUID.randomUUID();
    }

    @Test
    void createDocument_validDto_shouldCreateDocumentWithStatusOk() throws Exception {
        DocumentCreateDTO createDto = new DocumentCreateDTO(
                ID,
                "documentName",
                "asdasdt4wesdddxvcx",
                null
        );

        when(userInfoDetails.getId()).thenReturn(userId);

        performRequest(post(BASE_URI), createDto, userInfoDetails)
                .andDo(print())
                .andExpect(status().isCreated());

        verify(userInfoDetails).getId();
        verify(documentService).saveDocument(any(DocumentCreateDTO.class), eq(userId));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void createDocument_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        DocumentCreateDTO createDto = new DocumentCreateDTO(
                ID,
                name,
                "asdasdt4wesdddxvcx",
                null
        );

        performRequest(post(BASE_URI), createDto, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());


        verify(userInfoDetails, never()).getId();
        verify(documentService, never()).saveDocument(any(DocumentCreateDTO.class), eq(userId));
    }

    @Test
    void updateDocument_validDto_shouldUpdateWithStatusOk() throws Exception {
        DocumentUpdateDTO updateDto = new DocumentUpdateDTO(
                "name",
                "dasdaxzcdgs",
                null
        );

        when(userInfoDetails.getId()).thenReturn(userId);

        performRequest(put(BASE_URI + "/{id}", ID), updateDto, userInfoDetails)
                .andDo(print())
                .andExpect(status().isOk());

        verify(userInfoDetails).getId();
        verify(documentService).updateDocument(eq(ID), any(DocumentUpdateDTO.class), eq(userId));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void updateDocument_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        DocumentUpdateDTO updateDto = new DocumentUpdateDTO(
                name,
                "dasdaxzcdgs",
                null
        );

        performRequest(put(BASE_URI + "/{id}", ID), updateDto, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userInfoDetails, never()).getId();
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

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, userInfoDetails)
                .andDo(print())
                .andExpect(status().isOk());

        verify(approvementService).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(userInfoDetails));
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

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(userInfoDetails));
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

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(userInfoDetails));
    }

    @Test
    void startApprovement_userIdsIsEmpty_shouldReturnStatusBadRequest() throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                ID,
                LocalDateTime.now().plusDays(10),
                10,
                new HashSet<>()
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(userInfoDetails));
    }


    @Test
    void startApprovement_deadlineIsInThePast_shouldReturnStatusBadRequest() throws Exception {
        ApprovementProcessCreateDTO approvementProcessCreateDTO = new ApprovementProcessCreateDTO(
                ID,
                LocalDateTime.now().minusDays(10),
                10,
                new HashSet<>()
        );

        performRequest(post(BASE_URI + "/approvement/start"), approvementProcessCreateDTO, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(approvementService, never()).createApprovementProcess(any(ApprovementProcessCreateDTO.class), eq(userInfoDetails));
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
        return RandomStringUtils.random(INVALID_LENGTH);
    }
}
