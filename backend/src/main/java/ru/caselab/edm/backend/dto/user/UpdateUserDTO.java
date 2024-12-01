package ru.caselab.edm.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.enums.RoleName;

@Schema(description = "DTO for update user")
public record UpdateUserDTO(

        @Schema(description = "Login", example = "login")
        @Size(min = 5, max = 20, message = "Login must be between {min} and {max} character length")
        String login,

        @Schema(description = "Email", example = "email@email.com")
        @Email
        String email,

        @Schema(description = "First name", example = "first name")
        @Size(min = 2, max = 20, message = "First name must be between {min} and {max} character length")
        String firstName,

        @Schema(description = "Last name", example = "last name")
        @Size(min = 2, max = 20, message = "Last name must be between {min} and {max} character length")
        String lastName,

        @Schema(description = "Patronymic", example = "patronymic", nullable = true)
        @Size(max = 20, message = "Patronymic must not be greater than {max} character length")
        String patronymic,

        @Schema(description = "Position", example = "Developer")
        @Size(min = 2, max = 20, message = "Position must be between {min} and {max} character length")
        String position,

        @Schema(description = "Role", example = "[\"USER\", \"ADMIN\"]")
        RoleName[] roles) {
}
