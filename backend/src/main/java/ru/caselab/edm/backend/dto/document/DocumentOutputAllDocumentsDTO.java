package ru.caselab.edm.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;

import java.time.Instant;

@Data
@Schema(description = "DTO for get all document for user")
@AllArgsConstructor
@NoArgsConstructor
public class DocumentOutputAllDocumentsDTO {

    @Schema(description = "Last name author document")
    private String lastName;

    @Schema(description = "Timestamp of document creation")
    private Instant createdAt;

    @Schema(description = "Document name")
    private String documentName;

    private ApprovementProcessStatus status;
}
