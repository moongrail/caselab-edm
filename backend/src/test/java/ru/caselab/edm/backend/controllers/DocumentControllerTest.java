package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.mapper.documentversion.DocumentVersionMapper;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.service.ApprovementService;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.MinioService;
import ru.caselab.edm.backend.service.SignatureService;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

    private static final String BASE_URI = "/document";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final Long ID = 1L;
    private static final int INVALID_LENGTH = 256;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DocumentService documentService;
    @Mock
    private UserInfoDetails userInfoDetails;
    private ObjectMapper objectMapper;
    private UUID userId;

    @MockBean
    private SignatureService signatureService;
    @MockBean
    private DocumentVersionMapper documentVersionMapper;
    @MockBean
    private ApprovementService approvementService;
    @MockBean
    private MinioService minioService;
    @MockBean
    private AttributeSearchRepository attributeSearchRepository;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
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

        performPostRequest(createDto, userInfoDetails)
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

        performPostRequest(createDto, userInfoDetails)
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

        performPutRequest(ID, updateDto, userInfoDetails)
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

        performPutRequest(ID, updateDto, userInfoDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userInfoDetails, never()).getId();
        verify(documentService, never()).updateDocument(eq(ID), any(DocumentUpdateDTO.class), eq(userId));

    }

    private static Stream<Arguments> getNameValidationCases() {
        return Stream.of(
                arguments(named("Name is null", null)),
                arguments(named("Name is blank", null)),
                arguments(named("Name is too short", "t")),
                arguments(named("Name is too long", generateStringWithInvalidLength()))
        );
    }

    private static String generateStringWithInvalidLength() {
        return RandomStringUtils.random(INVALID_LENGTH);
    }

    private ResultActions performPostRequest(DocumentCreateDTO documentCreateDTO, UserInfoDetails userInfoDetails) throws Exception {
        return mockMvc.perform(
                post(BASE_URI)
                        .contentType(JSON)
                        .with(csrf())
                        .with(user(userInfoDetails))
                        .content(writeAsJson(documentCreateDTO))
        );
    }

    private ResultActions performPutRequest(Long documentId, DocumentUpdateDTO documentUpdateDTO, UserInfoDetails userInfoDetails) throws Exception {
        return mockMvc.perform(
                put(BASE_URI + "/{id}", documentId)
                        .contentType(JSON)
                        .with(csrf())
                        .with(user(userInfoDetails))
                        .content(writeAsJson(documentUpdateDTO))
        );
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

}
