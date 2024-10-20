package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Schema(description = "DTO for creating document type")
@Data
public class DocumentTypeCreateDTO {
    @Schema(description = "Document type name", example = "contract")
    @NotBlank
    private String name;

    @Schema(description = "Description", example = "text document with external contractors", nullable = true)
    private String description;

    @Schema(description = "Attributes", example = "[1, 2]", nullable = true)
    private Set<Long> attributeIds;
}
