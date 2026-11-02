package ru.caselab.edm.backend.shared.directory.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "DTO for representing a user available for delegation or replacement")
public record UserInfoDTO(
        @Schema(description = "ID", format = "uuid", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Login", example = "login")
        String login,
        @Schema(description = "Email", example = "email@email.com")
        String email,
        @Schema(description = "First name", example = "John")
        String firstName,
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        @Schema(description = "Patronymic", example = "Smith", nullable = true)
        String patronymic,
        @Schema(description = "Position", example = "Developer")
        String position,
        @Schema(description = "Roles")
        Set<RoleInfoDTO> roles
) {
}
