package ru.caselab.edm.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for representing download link of document")
public record DocumentLinkDTO(
        @Schema(description = "Download link", example = "http://localhost:9000/documents/01c01270-1e43-4bdd-94d9-3f07463b65cb/2024-11-18_17-34-29_%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%20%D0%BA%D1%80%D1%83%D1%82%D0%BE%D0%B9%20%D0%B2%D0%BE%D0%BE%D0%B1%D1%89%D0%B5?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minio-user%2F20241118%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20241118T151333Z&X-Amz-Expires=900&X-Amz-SignedHeaders=host&X-Amz-Signature=76f5b45254b7fbfbd4a1d4282d2d8eab948d7353cbb2e77c76c3555093874c4d")
        String link) {
}
