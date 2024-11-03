package ru.caselab.edm.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for update password")
public record UpdatePasswordDTO(

        @Schema(description = "Old password", example = "oldPassword")
        @NotBlank String oldPassword,

        @Schema(description = "New password", example = "newPassword")
        @NotBlank String newPassword) {
}
