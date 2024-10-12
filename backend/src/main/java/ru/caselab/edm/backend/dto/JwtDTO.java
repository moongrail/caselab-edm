package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for returning a new JWT and the associated refresh token after successful authentication/updating JWT")
public record JwtDTO(

        @Schema(description = "Refresh token", example = "2442d80c-1b4f-42fb-82d8-0c1b4fa2fb42")
        @NotBlank String refreshToken,

        @Schema(description = "JWT token", example = "ewogICJhbGciOiJIUzI1NiIKfQ.ewogICJhdXRob3JpdGllcyI6WwogICAgIlJPTEVfQURNSU4iCiAgXSwKICAic3ViIjoidGVzdCIsCiAgImlhdCI6MTcyODQxNjYxMCwKICAiZXhwIjoxNzI4NDIwMjEwCn0.fkxPtTFeyS3uxUIasibEd02H4A9hQqTWw1NIrD7g5C0")
        @NotBlank String jwt) {
}
