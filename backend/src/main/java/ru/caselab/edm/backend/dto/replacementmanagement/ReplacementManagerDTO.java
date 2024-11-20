package ru.caselab.edm.backend.dto.replacementmanagement;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "DTO for representing replacement")
public record ReplacementManagerDTO(
        @Schema(description = "ID of replacement", example = "1")
        Long id,

        @Schema(description = "ID of the user being replaced", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "ID of replacement user", example = "7b38e484-ed4b-4c49-8914-3dc22b054600")
        UUID replacementUserId,

        @Schema(description = "Start of the replacement", example = "2024-11-21T20:35:18")
        Instant startDate,

        @Schema(description = "End of the replacement", example = "2024-11-30T20:35:18")
        Instant endDate
) {
}
