package ru.caselab.edm.backend.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO for representing added/deleted/skipped members")
public record StatisticMembersDTO(

        @Schema(description = "Added/deleted members")
        List<UUID> added,

        @Schema(description = "Skipped members")
        List<UUID> skipped
) {
}
