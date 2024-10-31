package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Schema(description = "DTO for representing document version")
public class DocumentVersionDTO {
    @Schema(description = "id", format = "long", example = "1")
    private Long id;

    @Schema(description = "Document name", example = "Spongebob best episodes")
    private String documentName;

    @Schema(description = "Timestamp of document creation", example = "2024-10-17 22:01:36.778929+04")
    private Instant createdAt;

    @Schema(description = "Timestamp of document updation", example = "2024-10-17 22:01:36.778929+04")
    private Instant updatedAt;

    @Schema(description = "Content url", example = "")
    private String contentUrl;

    @Schema(description = "Id of the document whose version is the current document version", example = "1")
    private Long documentId;

    @Schema(description = "Value attributes", example = "[1, 2]")
    private List<DocumentAttributeValueDTO> attributeValues;
}