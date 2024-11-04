package ru.caselab.edm.backend.dto.approvementprocessitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(name = "DTO for representing approvement process item")
public class ApprovementProcessItemDTO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Approvement process ID", example = "1")
    private Long approvementProcessId;

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000", format = "uuid")
    private UUID userId;

    @Schema(description = "Date of creation", example = "2020-01-01T00:00:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "Process status", example = "APPROVED")
    private ApprovementProcessItemStatus status;

    @Schema(description = "Document version ID", example = "1")
    private Long documentVersionId;

    @Schema(description = "Signature ID", example = "1")
    private Long signatureId;
}
