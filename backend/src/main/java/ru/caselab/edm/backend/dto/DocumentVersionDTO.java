package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "DTO for representing document version")
public record DocumentVersionDTO(

        @Schema(description = "id", format = "long", example = "1")
        Long id,

        @Schema(description = "Document name", example = "Spongebob best episodes")
        String documentName,

        @Schema(description = "Timestamp of document creation", example = "2024-10-17 22:01:36.778929+04")
        Instant createdAt,

        @Schema(description = "Timestamp of document updation", example = "2024-10-17 22:01:36.778929+04")
        Instant updatedAt,

        @Schema(description = "Content url", example = "")
        String contentUrl,

        @Schema(description = "Id of the document whose version is the current document version", example = "1")
        Long documentId
) {
}
