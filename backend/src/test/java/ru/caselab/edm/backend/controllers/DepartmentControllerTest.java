package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import ru.caselab.edm.backend.dto.department.CreateDepartmentDTO;
import ru.caselab.edm.backend.dto.department.MembersDTO;
import ru.caselab.edm.backend.service.DelegationService;
import ru.caselab.edm.backend.service.DepartmentService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest extends BaseControllerTest {

    private static final String BASE_URI = "/department";
    private static final int INVALID_LENGTH = 256;
    private static final Long ID = 1L;

    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private DelegationService delegationService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void createDepartment_validDto_shouldCreateDepartmentWithStatusOk() throws Exception {
        CreateDepartmentDTO createDepartmentDTO = new CreateDepartmentDTO(
                "name",
                "descr",
                1L,
                "manager"
        );

        performRequest(post(BASE_URI), createDepartmentDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(departmentService).createDepartment(any(CreateDepartmentDTO.class));
    }

    @EmptySource
    @NullSource
    @ParameterizedTest
    void createDepartment_invalidManager_shouldReturnStatusBadRequest(String manager) throws Exception {
        CreateDepartmentDTO createDepartmentDTO = new CreateDepartmentDTO(
                "name",
                "descr",
                1L,
                manager
        );

        performRequest(post(BASE_URI), createDepartmentDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(departmentService, never()).createDepartment(any(CreateDepartmentDTO.class));
    }

    @MethodSource("getNameValidationCases")
    @ParameterizedTest
    void createDepartment_invalidName_shouldReturnStatusBadRequest(String name) throws Exception {
        CreateDepartmentDTO createDepartmentDTO = new CreateDepartmentDTO(
                name,
                "descr",
                1L,
                "test"
        );

        performRequest(post(BASE_URI), createDepartmentDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(departmentService, never()).createDepartment(any(CreateDepartmentDTO.class));
    }

    @Test
    void addMembersToDepartment_validDto_shouldAddWithStatusOk() throws Exception {
        List<UUID> members = List.of(UUID.randomUUID());
        MembersDTO membersDTO = new MembersDTO(members);

        performRequest(post(BASE_URI + "/add/{id}", ID), membersDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(departmentService).addMembersToDepartment(any(MembersDTO.class), eq(ID));
    }

    @NullSource
    @EmptySource
    @ParameterizedTest
    void addMembersToDepartment_invalidMembersList_shouldReturnStatusBadRequest(List<UUID> members) throws Exception {
        MembersDTO membersDTO = new MembersDTO(members);

        performRequest(post(BASE_URI + "/add/{id}", ID), membersDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(departmentService, never()).addMembersToDepartment(any(MembersDTO.class), eq(ID));
    }

    @Test
    void kickMembersFromDepartment_validDto_shouldKickWithStatusOk() throws Exception {
        List<UUID> members = List.of(UUID.randomUUID());
        MembersDTO membersDTO = new MembersDTO(members);

        performRequest(post(BASE_URI + "/kick/{id}", ID), membersDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(departmentService).kickMembersFromDepartment(any(MembersDTO.class), eq(ID));
    }

    @NullSource
    @EmptySource
    @ParameterizedTest
    void kickMembersFromDepartment_invalidMembersList_shouldReturnStatusBadRequest(List<UUID> members) throws Exception {
        MembersDTO membersDTO = new MembersDTO(members);

        performRequest(post(BASE_URI + "/kick/{id}", ID), membersDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(departmentService, never()).kickMembersFromDepartment(any(MembersDTO.class), eq(ID));
    }

    private static Stream<Arguments> getNameValidationCases() {
        return Stream.of(
                arguments(named("Name is blank", "  ")),
                arguments(named("Name is null", null)),
                arguments(named("Name is too short", "t")),
                arguments(named("Name is too long", generateStringWithInvalidLength()))
        );
    }

    private static String generateStringWithInvalidLength() {
        return RandomStringUtils.random(INVALID_LENGTH);
    }
}
