package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO for representing document attribute value")
public class DocumentAttributeValueDTO {

    @Schema(description = "Document attribute value id", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Attribute id", example = "1")
    private Long attributeId;

    @NotNull
    @Schema(description = "Document version id", example = "1")
    private Long documentId;

    @Schema(description = "value")
    private String value;
}
