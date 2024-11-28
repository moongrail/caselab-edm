package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import ru.caselab.edm.backend.builder.user.CreateUserDtoBuilder;
import ru.caselab.edm.backend.builder.user.UpdateUserDtoBuilder;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordForAdminDTO;
import ru.caselab.edm.backend.dto.user.UpdateUserDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.service.UserService;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(UserController.class)
public class UserControllerTest extends BaseControllerTest {

    private static final String BASE_URI = "/users";

    private static final int INVALID_FIRST_NAME_LENGTH = 21;
    private static final int INVALID_LAST_NAME_LENGTH = 21;
    private static final int INVALID_PATRONYMIC_LENGTH = 21;
    private static final int INVALID_LOGIN_LENGTH = 21;
    private static final int INVALID_POSITION_LENGTH = 21;


    @MockBean
    private UserService userService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userId = UUID.randomUUID();
    }

    @Test
    void createUser_validDto_shouldCreateUserWithStatusOk() throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .build();

        performRequest(post(BASE_URI), createUserDTO)
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

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getFirstNameValidationCases")
    @ParameterizedTest
    void createUser_invalidFirstName_shouldReturnStatusBadRequest(String firstName) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withFirstName(firstName).build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getLastNameValidationCases")
    @ParameterizedTest
    void createUser_invalidLastName_shouldReturnStatusBadRequest(String lastName) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withLastName(lastName).build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getPatronymicValidationCases")
    @ParameterizedTest
    void createUser_invalidPatronymic_shouldReturnStatusBadRequest(String patronymic) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPatronymic(patronymic)
                .build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getLoginValidationCases")
    @ParameterizedTest
    void createUser_invalidLogin_shouldReturnStatusBadRequest(String login) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withLogin(login).build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getPositionValidationCases")
    @ParameterizedTest
    void createUser_invalidPosition_shouldReturnStatusBadRequest(String position) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPosition(position)
                .build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getEmailValidationCases")
    @ParameterizedTest
    void createUser_invalidEmail_shouldReturnStatusBadRequest(String email) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder().withEmail(email).build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getRolesValidationCases")
    @ParameterizedTest
    void createUser_invalidRoles_shouldReturnStatusBadRequest(RoleName[] roles) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withRoles(roles)
                .build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getPasswordValidationCases")
    @ParameterizedTest
    void createUser_invalidPassword_shouldReturnStatusBadRequest(String password) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPassword(password)
                .withPasswordConfirmation(password)
                .build();

        performRequest(post(BASE_URI), createUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @Test
    void updatePassword_validDto_shouldUpdatePasswordWithStatusNoContent() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pa1!",
                "new-pa1!"
        );
        UserInfoDetails mockUserDetails = mock(UserInfoDetails.class);

        when(mockUserDetails.getId()).thenReturn(userId);

        performRequest(patch(BASE_URI + "/password"), updatePasswordDTO, mockUserDetails)
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(mockUserDetails).getId();
        verify(userService).updatePassword(eq(userId), any(UpdatePasswordDTO.class));
    }

    @Test
    void updatePassword_invalidPasswordConfirmation_shouldReturnStatusBadRequest() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pa1!",
                "new-pa1!-confirmation"
        );
        UserInfoDetails mockUserDetails = mock(UserInfoDetails.class);

        performRequest(patch(BASE_URI + "/password"), updatePasswordDTO, mockUserDetails)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(mockUserDetails, never()).getId();
        verify(userService, never()).updatePassword(any(UUID.class), any(UpdatePasswordDTO.class));
    }

    @Test
    void updatePasswordAsAdmin_validDto_shouldUpdatePasswordWithStatusNoContent() throws Exception {
        UpdatePasswordForAdminDTO updatePasswordForAdminDTO = new UpdatePasswordForAdminDTO(
                "pa1!word",
                "pa1!word"
        );

        performRequest(patch(BASE_URI + "/{userId}/password", userId), updatePasswordForAdminDTO)
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService).updatePasswordAsAdmin(eq(userId), any(UpdatePasswordForAdminDTO.class));
    }

    @Test
    void updatePasswordAsAdmin_invalidPasswordConfirmation_shouldReturnStatusBadRequest() throws Exception {
        UpdatePasswordForAdminDTO updatePasswordForAdminDTO = new UpdatePasswordForAdminDTO(
                "pa1!word",
                "pa1!word-confirmation"
        );

        performRequest(patch(BASE_URI + "/{userId}/password", userId), updatePasswordForAdminDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePasswordAsAdmin(eq(userId), any(UpdatePasswordForAdminDTO.class));
    }

    @MethodSource("getPasswordValidationCases")
    @ParameterizedTest
    void updatePasswordAsAdmin_invalidPassword_shouldReturnStatusBadRequest(String password) throws Exception {
        UpdatePasswordForAdminDTO updatePasswordForAdminDTO = new UpdatePasswordForAdminDTO(
                password,
                password
        );

        performRequest(patch(BASE_URI + "/{userId}/password", userId), updatePasswordForAdminDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePasswordAsAdmin(eq(userId), any(UpdatePasswordForAdminDTO.class));
    }

    @Test
    void updateUser_validDto_shouldUpdateUserWithStatusOk() throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getFirstNameValidationCases")
    @ParameterizedTest
    void updateUser_invalidFirstName_shouldReturnStatusBadRequest(String firstName) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withFirstName(firstName).build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getLastNameValidationCases")
    @ParameterizedTest
    void updateUser_invalidLastName_shouldReturnStatusBadRequest(String lastName) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withLastName(lastName).build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getPatronymicValidationCases")
    @ParameterizedTest
    void updateUser_invalidPatronymic_shouldReturnStatusBadRequest(String patronymic) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withPatronymic(patronymic).build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getLoginValidationCases")
    @ParameterizedTest
    void updateUser_invalidLogin_shouldReturnStatusBadRequest(String login) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withLogin(login).build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getPositionValidationCases")
    @ParameterizedTest
    void updateUser_invalidPosition_shouldReturnStatusBadRequest(String position) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withPosition(position).build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    @MethodSource("getEmailValidationCases")
    @ParameterizedTest
    void updateUser_invalidEmail_shouldReturnStatusBadRequest(String email) throws Exception {
        UpdateUserDTO updateUserDTO = UpdateUserDtoBuilder.builder().withEmail(email).build();

        performRequest(patch(BASE_URI + "/{userId}", userId), updateUserDTO)
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(eq(userId), any(UpdateUserDTO.class));
    }

    private static Stream<Arguments> getFirstNameValidationCases() {
        return Stream.of(
                arguments(named("FirstName is too short", "n")),
                arguments(named("FirstName is too long", generateStringWithLength(INVALID_FIRST_NAME_LENGTH)))
        );
    }

    private static Stream<Arguments> getLastNameValidationCases() {
        return Stream.of(
                arguments(named("LastName is too short", "n")),
                arguments(named("LastName is too long", generateStringWithLength(INVALID_LAST_NAME_LENGTH)))

        );
    }

    private static Stream<Arguments> getPatronymicValidationCases() {
        return Stream.of(
                arguments(named("Patronymic is too long", generateStringWithLength(INVALID_PATRONYMIC_LENGTH)))
        );
    }

    private static Stream<Arguments> getLoginValidationCases() {
        return Stream.of(
                arguments(named("Login is too short", "log")),
                arguments(named("Login is too long", generateStringWithLength(INVALID_LOGIN_LENGTH)))
        );
    }

    private static Stream<Arguments> getPositionValidationCases() {
        return Stream.of(
                arguments(named("Position is too short", "p")),
                arguments(named("Position is too long", generateStringWithLength(INVALID_POSITION_LENGTH)))
        );
    }

    private static Stream<Arguments> getEmailValidationCases() {
        return Stream.of(
                arguments(named("Email is invalid", "invalid-email"))
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

    private static String generateStringWithLength(int length) {
        return RandomStringUtils.random(length);
    }
}
