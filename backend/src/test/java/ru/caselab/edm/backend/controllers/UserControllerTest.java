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

    @MethodSource("getInvalidCreateUserDto")
    @ParameterizedTest
    void createUser_invalidDto_shouldReturnStatusBadRequest(CreateUserDTO createUserDTO) throws Exception {
        perfomPostRequest(createUserDTO)
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

    private static Stream<Arguments> getInvalidCreateUserDto() {
        return Stream.of(
                arguments(named("With blank firstname",
                        CreateUserDtoBuilder.builder().withFirstName(" ").build())),
                arguments(named("With null firstname",
                        CreateUserDtoBuilder.builder().withFirstName(null).build())),
                arguments(named("With blank lastname",
                        CreateUserDtoBuilder.builder().withLastName(" ").build())),
                arguments(named("With null lastname",
                        CreateUserDtoBuilder.builder().withLastName(null).build())),
                arguments(named("With blank login",
                        CreateUserDtoBuilder.builder().withLogin(" ").build())),
                arguments(named("With null login",
                        CreateUserDtoBuilder.builder().withLogin(null).build())),
                arguments(named("With too short login",
                        CreateUserDtoBuilder.builder().withLogin("log").build())),
                arguments(named("With too long login",
                        CreateUserDtoBuilder.builder().withLogin("looooooooooooooooooooooooooogin").build())),
                arguments(named("With invalid email format",
                        CreateUserDtoBuilder.builder().withEmail("invalid-email").build())),
                arguments(named("With blank email",
                        CreateUserDtoBuilder.builder().withEmail(" ").build())),
                arguments(named("With null email",
                        CreateUserDtoBuilder.builder().withEmail(null).build())),
                arguments(named("With null departmentId",
                        CreateUserDtoBuilder.builder().withDepartmentId(null).build())),
                arguments(named("With null roles",
                        CreateUserDtoBuilder.builder().withRoles(null).build())),
                arguments(named("With empty roles",
                        CreateUserDtoBuilder.builder().withRoles(new RoleName[0]).build())),
                arguments(named("With invalid password format",
                        CreateUserDtoBuilder.builder().withPassword("pass").withPasswordConfirmation("pass").build())),
                arguments(named("With too short password",
                        CreateUserDtoBuilder.builder().withPassword("p1!").withPasswordConfirmation("p1!").build())),
                arguments(named("With too long password",
                        CreateUserDtoBuilder.builder()
                                .withPassword("p1!ppppppppppppppppppppppppppppppppppppppppppppppppppppp")
                                .withPasswordConfirmation("p1!ppppppppppppppppppppppppppppppppppppppppppppppppppppp")
                                .build())
                ),
                arguments(named("With blank password",
                        CreateUserDtoBuilder.builder().withPassword(" ").withPasswordConfirmation(" ").build())),
                arguments(named("With null password",
                        CreateUserDtoBuilder.builder().withPassword(null).withPasswordConfirmation(null).build())),
                arguments(named("With mismatched password confirmation",
                        CreateUserDtoBuilder.builder().withPassword("Password123!")
                                .withPasswordConfirmation("DifferentPassword123!").build())),
                arguments(named("With blank position",
                        CreateUserDtoBuilder.builder().withPosition(" ").build())),
                arguments(named("With null position",
                        CreateUserDtoBuilder.builder().withPosition(null).build()))
        );
    }
}
