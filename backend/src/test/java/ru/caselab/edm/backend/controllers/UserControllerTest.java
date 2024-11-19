package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.caselab.edm.backend.builder.user.CreateUserDtoBuilder;
import ru.caselab.edm.backend.builder.user.UpdateUserDtoBuilder;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordForAdminDTO;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.service.UserService;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(UserController.class)
public class UserControllerTest {

    private static final String BASE_URI = "/users";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    private ObjectMapper objectMapper;
    private UUID userId;

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
    void createUser_validDto_shouldCreateUser() throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    void createUser_passwordsDoNotMatch_shouldReturnStatusBadRequest() throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPassword("pass1!word")
                .withPasswordConfirmation("pass1!wordConfirmation")
                .build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getFirstNameValidationCases")
    @ParameterizedTest
    void createUser_invalidFirstName_shouldReturnBadRequest(String firstName) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withFirstName(firstName).build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getLastNameValidationCases")
    @ParameterizedTest
    void createUser_invalidLastName_shouldReturnBadRequest(String lastName) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withLastName(lastName).build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getPatronymicValidationCases")
    @ParameterizedTest
    void createUser_invalidPatronymic_shouldReturnBadRequest(String patronymic) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPatronymic(patronymic)
                .build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getLoginValidationCases")
    @ParameterizedTest
    void createUser_invalidLogin_shouldReturnBadRequest(String login) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withLogin(login).build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getPositionValidationCases")
    @ParameterizedTest
    void createUser_invalidPosition_shouldReturnBadRequest(String position) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPosition(position)
                .build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getEmailValidationCases")
    @ParameterizedTest
    void createUser_invalidEmail_shouldReturnBadRequest(String email) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withEmail(email).build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getRolesValidationCases")
    @ParameterizedTest
    void createUser_invalidRoles_shouldReturnBadRequest(RoleName[] roles) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withRoles(roles)
                .build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getPasswordValidationCases")
    @ParameterizedTest
    void createUser_invalidPassword_shouldReturnBadRequest(String password) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPassword(password)
                .withPasswordConfirmation(password)
                .build();

        performPostRequest(createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @Test
    void updatePassword_validDto_shouldUpdatePassword() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pa1!",
                "new-pa1!"
        );
        UserInfoDetails mockUserDetails = mock(UserInfoDetails.class);

        when(mockUserDetails.getId()).thenReturn(userId);

        performPatchRequest(mockUserDetails, updatePasswordDTO)
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(mockUserDetails).getId();
        verify(userService).updatePassword(eq(userId), any(UpdatePasswordDTO.class));
    }

    @Test
    void updatePassword_invalidPasswordConfirmation_shouldUpdatePassword() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pa1!",
                "new-pa1!-confirmation"
        );
        UserInfoDetails mockUserDetails = mock(UserInfoDetails.class);

        performPatchRequest(mockUserDetails, updatePasswordDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(mockUserDetails, never()).getId();
        verify(userService, never()).updatePassword(any(UUID.class), any(UpdatePasswordDTO.class));
    }

    @Test
    void updateUser_validDto_shouldUpdateUserWithStatusOk() throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getFirstNameValidationCases")
    @ParameterizedTest
    void updateUser_invalidFirstName_shouldReturnBadRequest(String firstName) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withFirstName(firstName).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getLastNameValidationCases")
    @ParameterizedTest
    void updateUser_invalidLastName_shouldReturnBadRequest(String lastName) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withLastName(lastName).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getPatronymicValidationCases")
    @ParameterizedTest
    void updateUser_invalidPatronymic_shouldReturnBadRequest(String patronymic) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withPatronymic(patronymic).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getLoginValidationCases")
    @ParameterizedTest
    void updateUser_invalidLogin_shouldReturnBadRequest(String login) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withLogin(login).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getPositionValidationCases")
    @ParameterizedTest
    void updateUser_invalidPosition_shouldReturnBadRequest(String position) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withPosition(position).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getEmailValidationCases")
    @ParameterizedTest
    void updateUser_invalidEmail_shouldReturnBadRequest(String email) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withEmail(email).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getRolesValidationCases")
    @ParameterizedTest
    void updateUser_invalidRoles_shouldReturnBadRequest(RoleName[] roles) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withRoles(roles).build();

        performPutRequest(userId, updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }


    private static Stream<Arguments> getFirstNameValidationCases() {
        return Stream.of(
                arguments(named("FirstName is blank", " ")),
                arguments(named("FirstName is null", null)),
                arguments(named("FirstName is too short", "n")),
                arguments(named("FirstName is too long", "firstnaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame"))
        );
    }

    private static Stream<Arguments> getLastNameValidationCases() {
        return Stream.of(
                arguments(named("LastName is blank", " ")),
                arguments(named("LastName is null", null)),
                arguments(named("LastName is too short", "n")),
                arguments(named("LastName is too long", "lastnaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame"))

        );
    }

    private static Stream<Arguments> getPatronymicValidationCases() {
        return Stream.of(
                arguments(named("Patronymic is too long", "patrooooooooooooooooooooooooooooooooooooooo"))
        );
    }

    private static Stream<Arguments> getLoginValidationCases() {
        return Stream.of(
                arguments(named("Login is blank", " ")),
                arguments(named("Login is null", null)),
                arguments(named("Login is too short", "log")),
                arguments(named("Login is too long", "looooooooooooooooooooooooooogin"))
        );
    }

    private static Stream<Arguments> getPositionValidationCases() {
        return Stream.of(
                arguments(named("Position is blank", " ")),
                arguments(named("Position is null", null)),
                arguments(named("Position is too short", "p")),
                arguments(named("Position is too long", "positiooooooooooooooooooooooooooooooooooooon"))
        );
    }

    private static Stream<Arguments> getEmailValidationCases() {
        return Stream.of(
                arguments(named("Email is invalid", "invalid-email")),
                arguments(named("Email is blank", " ")),
                arguments(named("Email is null", null))
        );
    }

    private static Stream<Arguments> getPasswordValidationCases() {
        return Stream.of(
                arguments(named("Password is too short", "p!1")),
                arguments(named("Password is too long",
                        "p1!ppppppppppppppppppppppppppppppppppppppppppppppppppppp"))
        );
    }

    private static Stream<Arguments> getRolesValidationCases() {
        return Stream.of(
                arguments(named("Roles are null", null)),
                arguments(named("Roles are empty", new RoleName[0]))
        );
    }

    private ResultActions performPatchRequest(UserInfoDetails userInfoDetails, UpdatePasswordDTO updatePasswordDTO) throws Exception {
        return mockMvc.perform(
                patch(BASE_URI + "/password", userId)
                        .contentType(JSON)
                        .with(csrf())
                        .with(user(userInfoDetails))
                        .content(writeAsJson(updatePasswordDTO))
        );
    }

    private ResultActions performPostRequest(CreateUserDTO createUserDTO) throws Exception {
        return mockMvc.perform(
                post(BASE_URI)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(createUserDTO))
        );

    }

    private ResultActions performPutRequest(UUID userId, UpdateUserDTO updateUserDTO) throws Exception {
        return mockMvc.perform(
                put(BASE_URI + "/{userId}", userId)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(updateUserDTO))
        );
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
