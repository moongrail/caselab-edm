package ru.caselab.edm.backend.dto.attributevalue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO for representing document attribute value")
public class DocumentAttributeValueDTO {

    @NotNull
    @Schema(description = "Attribute id", example = "1")
    private Long attributeId;

    private String value;
}
