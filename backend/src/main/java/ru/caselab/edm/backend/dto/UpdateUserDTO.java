package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for update user")
public record UpdateUserDTO(
        @Schema(description = "Login", example = "login")
        @NotBlank String login,

        @Schema(description = "Email", example = "email@email.com")
        @NotBlank @Email String email,

        @Schema(description = "First name", example = "first name")
        @NotBlank String firstName,

        @Schema(description = "Last name", example = "last name")
        @NotBlank String lastName,

        @Schema(description = "Patronymic", example = "patronymic", nullable = true)
        String patronymic) {
}
