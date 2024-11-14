package ru.caselab.edm.backend.dto.replacementmanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "DTO for representing user for replacement")
public class UsersForReplacementDTO {
    @Schema(description = "ID", format = "uuid", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    @Schema(description = "Login", example = "login")
    private String login;

    @Schema(description = "Email", example = "email@email.com")
    private String email;

    @Schema(description = "Password", example = "password")
    private String firstName;

    @Schema(description = "Last name", example = "lastName")
    private String lastName;

    @Schema(description = "Patronymic", example = "patronymic", nullable = true)
    private String patronymic;

    @Schema(description = "Position", example = "Developer")
    private  String position;
}
