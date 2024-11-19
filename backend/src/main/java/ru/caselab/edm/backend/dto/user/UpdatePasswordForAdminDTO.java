package ru.caselab.edm.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for update password")
public record UpdatePasswordForAdminDTO(

        @Schema(description = "New password", example = "newPassword")
        @NotBlank String newPassword) {
}
