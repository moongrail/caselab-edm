package ru.caselab.edm.backend.dto.documenttype;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Schema(description = "DTO for update document type")
@Data
public class DocumentTypeUpdateDTO {
    @Schema(description = "Document type name", example = "contract")
    private String name;

    @Schema(description = "Description document type", example = "text document with external contractors", nullable = true)
    private JsonNullable<String> description;

    @Schema(description = "Attributes", example = "[1, 2]", nullable = true)
    private Set<Long> attributeIds;

}
