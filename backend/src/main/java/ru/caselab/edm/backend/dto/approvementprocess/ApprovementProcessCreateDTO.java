package ru.caselab.edm.backend.dto.approvementprocess;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ApprovementProcessCreateDTO {

    @Schema(description = "Document ID", required = true, example = "1")
    @NotNull
    private Long documentId;

    @Schema(description = "Deadline of voting", example = "2024-11-03T20:35:18")
    @NotNull
    @Future
    private LocalDateTime deadline;

    @Schema(description = "Percentage of \"APPROVED\" votes to complete voting")
    @Min(value = 1, message = "Agreement percentage must be at least {value}")
    @Max(value = 100, message = "Agreement percentage must not exceed {value}")
    private float agreementPercent;

    @Schema(description = "List of IDs of voting participants")
    @NotEmpty
    private Set<UUID> usersIds;
}
