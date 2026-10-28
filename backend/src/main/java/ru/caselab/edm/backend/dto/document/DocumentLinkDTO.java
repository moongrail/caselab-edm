package ru.caselab.edm.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for representing download link of document")
public record DocumentLinkDTO(
        @Schema(description = "Download link", example = "http://localhost:9000/documents/<object-name>?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=900&X-Amz-Signature=<signature>")
        String link) {
}
