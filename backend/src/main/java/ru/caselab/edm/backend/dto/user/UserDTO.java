package ru.caselab.edm.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.caselab.edm.backend.dto.role.RoleDTO;
import ru.caselab.edm.backend.entity.Department;

import java.util.Set;
import java.util.UUID;

@Schema(description = "DTO for representing user")
public record UserDTO(

        @Schema(description = "ID", format = "uuid", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "Login", example = "login")
        String login,

        @Schema(description = "Email", example = "email@email.com")
        String email,

        @Schema(description = "Password", example = "password")
        String firstName,

        @Schema(description = "Last name", example = "lastName")
        String lastName,

        @Schema(description = "Patronymic", example = "patronymic", nullable = true)
        String patronymic,

        @Schema(description = "Position", example = "Developer")
        String position,

        @Schema(description = "Roles", example = "[\"USER\", \"ADMIN\"]")
        Set<RoleDTO> roles) {
}
