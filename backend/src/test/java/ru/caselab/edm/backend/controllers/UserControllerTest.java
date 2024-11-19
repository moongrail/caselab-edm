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
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    }

    @Test
    void createUser_validDto_shouldCreateUser() throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .build();

        perfomPostRequest(createUserDTO)
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

        perfomPostRequest(createUserDTO)
                .andDo(print()).andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @MethodSource("getInvalidPasswords")
    @ParameterizedTest
    void createUser_invalidPassword_shouldReturnStatusBadRequest(String invalidPassword) throws Exception {
        CreateUserDTO createUserDTO = CreateUserDtoBuilder.builder()
                .withPassword(invalidPassword)
                .build();

        perfomPostRequest(createUserDTO)
                .andDo(print()).andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));

    }

    @Test
    void updatePassword_validDto_shouldUpdatePassword() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pa1!",
                "new-pa1!"
        );
        UUID userId = UUID.randomUUID();

        perfomPatchRequest(userId, updatePasswordDTO)
                .andDo(print()).andExpect(status().isNoContent());

        verify(userService).updatePassword(userId, updatePasswordDTO);
    }

    @Test
    void updatePassword_invalidPasswordConfirmation_shouldUpdatePassword() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pa1!",
                "new-pa1!-confirmation"
        );
        UUID userId = UUID.randomUUID();

        perfomPatchRequest(userId, updatePasswordDTO)
                .andDo(print()).andExpect(status().isBadRequest());

        verify(userService, never()).updatePassword(eq(userId), any(UpdatePasswordDTO.class));
    }

    @MethodSource("getInvalidPasswords")
    @ParameterizedTest
    void updatePassword_invalidPassword_shouldReturnStatusBadRequest(String invalidPassword) throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                invalidPassword,
                invalidPassword
        );
        UUID userId = UUID.randomUUID();

        perfomPatchRequest(userId, updatePasswordDTO)
                .andDo(print()).andExpect(status().isBadRequest());

        verify(userService, never()).updatePassword(eq(userId), any(UpdatePasswordDTO.class));

    }

    private static Stream<Arguments> getInvalidPasswords() {
        return Stream.of(
                arguments(named("Without special character", "pas1word")),
                arguments(named("Without digits", "pas!word")),
                arguments(named("Too short", "pa1!")),
                arguments(named("Too long", "pa1!wordpa1!wordpa1!wordpa1!wordpa1!wordpa1!wordpa1!wordpa1!word"))

        );
    }

    private ResultActions perfomPatchRequest(UUID userId, UpdatePasswordDTO updatePasswordDTO) throws Exception {
        return mockMvc.perform(
                patch(BASE_URI + "/{userId}/password", userId)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(updatePasswordDTO))
        );

    }

    private ResultActions perfomPostRequest(CreateUserDTO createUserDTO) throws Exception {
        return mockMvc.perform(
                post(BASE_URI)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(createUserDTO))
        );

    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
