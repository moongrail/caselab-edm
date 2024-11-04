package ru.caselab.edm.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for refresh token")
public record RefreshTokenDTO(

        @Schema(description = "Refresh token", example = "2442d80c-1b4f-42fb-82d8-0c1b4fa2fb42")
        @NotBlank String refreshToken) {
}
