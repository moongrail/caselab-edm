package ru.caselab.edm.backend.dto.approvementprocess;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovementProcessDTO {

    @Schema(description = "Approvement process ID ")
    private Long id;

    @Schema(description = "Document ID")
    private Long documentId;

    @Schema(description = "Approvement Proccess status")
    private ApprovementProcessStatus status;

    @Schema(description = "Deadline of approvement process")
    private LocalDateTime deadline;

    @Schema(description = "Percentage of \"APPROVED\" votes to complete approvement process", example = "100")
    private float agreementPercent;

    @Schema(description = "List of IDs of approvement process items")
    private List<Long> approvementProcessItemsIds;
}
