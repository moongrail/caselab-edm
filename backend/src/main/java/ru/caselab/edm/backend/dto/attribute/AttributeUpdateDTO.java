package ru.caselab.edm.backend.dto.attribute;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "DTO for update attribute")
public class AttributeUpdateDTO {

    @Schema(description = "Attribute name", example = "attribute")
    @NotBlank
    @Size(min = 1, max = 255, message = "Attribute name must be between {min} and {max} character length")
    private String name;

    @Schema(description = "Attribute data type", example = "pdf")
    @NotBlank
    @Size(min = 1, max = 255, message = "Attribute data type must be between {min} and {max} character length")
    private String dataType;

    @Schema(description = "Is it required attribute", example = "true")
    private boolean isRequired;

    @Schema(description = "DocumentType id's")
    private Set<Long> documentTypeIds;
}
