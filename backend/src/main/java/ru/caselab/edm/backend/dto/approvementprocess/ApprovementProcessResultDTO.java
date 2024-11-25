package ru.caselab.edm.backend.dto.approvementprocess;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class ApprovementProcessResultDTO {

    @Schema(description = "Document ID", required = true, example = "1")
    private Long documentId;

    @Schema(description = "Deadline of voting", example = "2024-11-03T20:35:18")
    @NotNull
    private LocalDateTime deadline;

    @Schema(description = "Percentage of \"APPROVED\" votes to complete voting")
    private float agreementPercent;

    @NotNull
    @Schema(description = "Approvement Proccess status")
    private ApprovementProcessStatus status;
}
