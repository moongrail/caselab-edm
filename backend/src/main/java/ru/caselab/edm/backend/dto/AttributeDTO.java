package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "DTO for representing attribute")
public class AttributeDTO {

    @NotNull
    @Schema(description = "Attribute id")
    private Long id;

    @NotBlank
    @Schema(description = "Attribute name", example = "attribute")
    private String name;

    @NotBlank
    @Schema(description = "Attribute data type", example = "pdf")
    private String dataType;

    @Schema(description = "Is it required attribute", example = "true")
    private boolean isRequired;

    @Schema(description = "DocumentType id's")
    private Set<Long> documentTypeIds;
}
