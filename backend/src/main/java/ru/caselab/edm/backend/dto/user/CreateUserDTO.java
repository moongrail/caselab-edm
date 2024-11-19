package ru.caselab.edm.backend.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.caselab.edm.backend.enums.RoleName;
import ru.caselab.edm.backend.validation.annotations.Password;
import ru.caselab.edm.backend.validation.annotations.PasswordMatcher;
import ru.caselab.edm.backend.validation.interfaces.PasswordValidatable;

@Schema(description = "DTO for creating user")
@PasswordMatcher
public record CreateUserDTO(

        @Schema(description = "Department id", example = "1")
        @NotNull Long departmentId,

        @Schema(description = "Login", example = "login")
        @NotBlank
        @Size(min = 5, max = 20, message = "Login must be between {min} and {max} length")
        String login,

        @Schema(description = "Email", example = "email@email.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Password", example = "password")
        @NotBlank
        @Password
        String password,

        @Schema(description = "Password confirmation", example = "password")
        @NotBlank
        String passwordConfirmation,

        @Schema(description = "First name", example = "first name")
        @NotBlank
        String firstName,

        @Schema(description = "Last name", example = "last name")
        @NotBlank
        String lastName,

        @Schema(description = "Patronymic", example = "patronymic", nullable = true)
        String patronymic,

        @Schema(description = "Position", example = "Developer")
        @NotBlank
        String position,

        @Schema(description = "Role", example = "[\"USER\", \"ADMIN\"]")
        @NotNull
        @NotEmpty(message = "User must contains at least 1 role")
        RoleName[] roles

) implements PasswordValidatable {
        @Override
        public String getPassword() {
                return this.password;
        }

        @Override
        public String getPasswordConfirmation() {
                return this.passwordConfirmation;
        }
}
