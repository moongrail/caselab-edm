package ru.caselab.edm.backend.dto.replacementmanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "DTO for creating replacement")
public record CreateReplacementDTO(
        @Schema(description = "Replacement user ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull
        UUID userId,

        @Schema(description = "Start of the replacement", example = "2024-11-21T20:35:18")
        @NotNull
        @FutureOrPresent
        Instant start,

        @Schema(description = "Start of the replacement", example = "2024-11-30T20:35:18")
        @NotNull
        @Future
        Instant end) {
}
