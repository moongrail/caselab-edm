package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "DTO for create attribute value when create document")
@Data
public class AttributeValuetoCreateDocumentDTO {
    @Schema(description = "Id attribute", example = "1L")
    @NotNull
    private Long attributeId;

    @Schema(description = "Attribute value", example = "Some value according to the attribute type")
    private String value;
}
