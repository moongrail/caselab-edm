package ru.caselab.edm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.caselab.edm.backend.dto.user.CreateUserDTO;
import ru.caselab.edm.backend.dto.user.UpdatePasswordDTO;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.security.service.JwtService;
import ru.caselab.edm.backend.service.UserService;

import java.util.UUID;

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
        CreateUserDTO createUserDTO = new CreateUserDTO(
                1L,
                "test-login",
                "test@gmail.com",
                "test-password",
                "test-password",
                "test-name",
                "test-name",
                "test-patronymic",
                "test-position",
                new RoleName[]{RoleName.USER}
        );

        mockMvc.perform(
                post(BASE_URI)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(createUserDTO))
        ).andDo(print())
                .andExpect(status().isOk());

        verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    void createUser_passwordsDoNotMatch_shouldReturnStatusBadRequest() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                1L,
                null,
                "test@gmail.com",
                "password",
                "confirmation",
                "test-name",
                "test-name",
                "test-patronymic",
                "test-position",
                new RoleName[]{RoleName.USER}
        );

        mockMvc.perform(
                post(BASE_URI)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(createUserDTO))
        ).andDo(print()).andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserDTO.class));
    }

    @Test
    void updatePassword_validDto_shouldUpdatePassword() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pass",
                "new-pass"
        );
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                patch(BASE_URI + "/{userId}/password", userId)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(updatePasswordDTO))
        ).andDo(print()).andExpect(status().isNoContent());

        verify(userService).updatePassword(userId, updatePasswordDTO);
    }

    @Test
    void updatePassword_invalidPasswordConfirmation_shouldUpdatePassword() throws Exception {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "old-pass",
                "new-pass",
                "dummy"
        );
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                patch(BASE_URI + "/{userId}/password", userId)
                        .contentType(JSON)
                        .with(csrf())
                        .content(writeAsJson(updatePasswordDTO))
        ).andDo(print()).andExpect(status().isBadRequest());

        verify(userService, never()).updatePassword(eq(userId), any(UpdatePasswordDTO.class));
    }

    private String writeAsJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
