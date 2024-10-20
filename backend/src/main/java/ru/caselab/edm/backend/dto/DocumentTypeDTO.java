package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;


@Data
@Schema(description = "DTO for representing document type")
public class DocumentTypeDTO {
    @Schema(description = "Document type ID", example = "1")
    private Long id;

    @Schema(description = "Document type name", example = "contract")
    private String name;

    @Schema(description = "Description", example = "text document with external contractors", nullable = true)
    private String description;

    @Schema(description = "Attributes", example = "[1, 2]", nullable = true)
    private Set<Long> attributeIds;
}
