package ru.caselab.edm.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;

@Data
@Schema(description = "DTO for get all document for user")
@AllArgsConstructor
@NoArgsConstructor
public class DocumentOutputAllDocumentsDTO {

    @Schema(description = "Document ID")
    private Long id;

    @Schema(description = "Login author document")
    private String author;

    @Schema(description = "Timestamp of document creation")
    private Instant createdAt;

    @Schema(description = "Document name")
    private String documentName;

    @Schema(description = "Content URL")
    private String contentUrl;

    @Schema(description = "Document status")
    private DocumentStatus state;
}
/*
    @Schema(description = "Document approvement process status")
    private ApprovementProcessStatus approvementProcessStatus;*/