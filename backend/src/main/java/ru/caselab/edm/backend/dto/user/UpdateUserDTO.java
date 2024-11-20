package ru.caselab.edm.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.caselab.edm.backend.enums.RoleName;

@Schema(description = "DTO for update user")
public record UpdateUserDTO(
        @Schema(description = "Login", example = "login")
        String login,

        @Schema(description = "Email", example = "email@email.com")
        @Email String email,

        @Schema(description = "First name", example = "first name")
        String firstName,

        @Schema(description = "Last name", example = "last name")
        String lastName,

        @Schema(description = "Patronymic", example = "patronymic", nullable = true)
        String patronymic,
        @Schema(description = "Role", example = "[\"USER\", \"ADMIN\"]")
        RoleName[] roles) {
}
