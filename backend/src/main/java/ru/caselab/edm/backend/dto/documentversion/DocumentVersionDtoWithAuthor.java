package ru.caselab.edm.backend.dto.documentversion;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;
import java.util.List;

@Data
@Schema(description = "DTO for representing document version with author")
public class DocumentVersionDtoWithAuthor {
    @Schema(description = "id", format = "long", example = "1")
    @JsonProperty("documentVersionId")
    private Long id;

    @Schema(description = "Login of author", format = "String", example = "nof-the-ceo")
    private String author;

    @Schema(description = "Document name", example = "Spongebob best episodes")
    private String documentName;

    @Schema(description = "Timestamp of document creation", example = "2024-10-17 22:01:36.778929+04")
    private Instant createdAt;

    @Schema(description = "Content url", example = "")
    private String contentUrl;

    @Schema(description = "General document status", example = "DRAFT")
    private DocumentStatus state;

    @Schema(description = "Id of the document whose version is the current document version", example = "1")
    private Long documentId;

    @Schema(description = "Value attributes", example = "[1, 2]")
    private List<DocumentAttributeValueDTO> attributeValues;
}