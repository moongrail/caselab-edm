package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.caselab.edm.backend.dto.attribute.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.attribute.AttributeUpdateDTO;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.service.AttributeService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(AttributeController.class)
public class AttributeControllerTest {

    private static final String BASE_URI = "/attributes";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final Long ATTR_ID = 1L;
    private static final int INVALID_LENGTH = 256;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AttributeService attributeService;
    private ObjectMapper objectMapper;

    @MockBean
    private AttributeSearchRepository attributeSearchRepository;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private JwtService jwtService;

    @BeforeEach()
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void createAttribute_validDto_shouldCreateWithStatusCreated() throws Exception {
        AttributeCreateDTO createDto = new AttributeCreateDTO(
                "name",
                "data",
                true,
                null
        );

        performPostRequest(createDto)
                .andDo(print())
                .andExpect(status().isCreated());

        verify(attributeService).createAttribute(any(AttributeCreateDTO.class));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void createAttribute_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        AttributeCreateDTO createDto = new AttributeCreateDTO(
                name,
                "data",
                true,
                null
        );

        performPostRequest(createDto)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(attributeService, never()).createAttribute(any(AttributeCreateDTO.class));
    }

    @MethodSource("getDataTypeValidationCases")
    @ParameterizedTest
    void createAttribute_invalidDataType_shouldReturnStatusBadRequest(String dataType) throws Exception {
        AttributeCreateDTO createDto = new AttributeCreateDTO(
                "name",
                dataType,
                true,
                null
        );

        performPostRequest(createDto)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(attributeService, never()).createAttribute(any(AttributeCreateDTO.class));
    }

    @Test
    void updateAttribute_validDto_shouldUpdateWithStatusOk() throws Exception {
        AttributeUpdateDTO updateDTO = new AttributeUpdateDTO(
                "name",
                "data",
                true,
                null
        );

        performPutRequest(ATTR_ID, updateDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(attributeService).updateAttribute(eq(ATTR_ID), any(AttributeUpdateDTO.class));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void updateAttribute_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        AttributeUpdateDTO updateDTO = new AttributeUpdateDTO(
                name,
                "data",
                true,
                null
        );

        performPutRequest(ATTR_ID, updateDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(attributeService, never()).updateAttribute(eq(ATTR_ID), any(AttributeUpdateDTO.class));
    }

    @MethodSource("getDataTypeValidationCases")
    @ParameterizedTest
    void updateAttribute_invalidDataType_shouldReturnStatusBadRequest(String dataType) throws Exception {
        AttributeUpdateDTO updateDTO = new AttributeUpdateDTO(
                "name",
                dataType,
                true,
                null
        );

        performPutRequest(ATTR_ID, updateDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(attributeService, never()).updateAttribute(eq(ATTR_ID), any(AttributeUpdateDTO.class));
    }

    private static Stream<Arguments> getNameValidationCases() {
        return Stream.of(
                arguments(named("Name is blank", " ")),
                arguments(named("Name is null", null)),
                arguments(named("Name is too short", "n")),
                arguments(named("Name is too long", generateStringWithInvalidLength()))
        );
    }

    private static Stream<Arguments> getDataTypeValidationCases() {
        return Stream.of(
                arguments(named("Data type is blank", " ")),
                arguments(named("Data type is null", null)),
                arguments(named("Data type is too short", "n")),
                arguments(named("Data type is too long", generateStringWithInvalidLength()))
        );
    }

    private static String generateStringWithInvalidLength() {
        return RandomStringUtils.random(INVALID_LENGTH);
    }

    private ResultActions performPostRequest(AttributeCreateDTO attributeCreateDTO) throws Exception {
        return mockMvc.perform(
                post(BASE_URI)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(attributeCreateDTO))
        );
    }

    private ResultActions performPutRequest(Long attributeId, AttributeUpdateDTO attributeUpdateDTO) throws Exception {
        return mockMvc.perform(
                put(BASE_URI + "/{id}", attributeId)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(attributeUpdateDTO))
        );
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

}
