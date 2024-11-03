package ru.caselab.edm.backend.dto.attributevalue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentAttributeValueUpdateDTO {
    @Schema(description = "Id attribute", example = "1L")
    @NotNull
    private Long attributeId;

    @Schema(description = "Attribute value", example = "Some value according to the attribute type")
    private String value;
}
