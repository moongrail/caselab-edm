package ru.caselab.edm.backend.document.api.dto.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Schema(description = "DTO for get all document for user")
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

    @JsonIgnore
    private UUID ownerId;

    public DocumentOutputAllDocumentsDTO(Long id, String author, Instant createdAt,
                                         String documentName, String contentUrl,
                                         DocumentStatus state) {
        this(id, author, createdAt, documentName, contentUrl, state, null);
    }

    public DocumentOutputAllDocumentsDTO(Long id, String author, Instant createdAt,
                                         String documentName, String contentUrl,
                                         DocumentStatus state, UUID ownerId) {
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        this.documentName = documentName;
        this.contentUrl = contentUrl;
        this.state = state;
        this.ownerId = ownerId;
    }
}
