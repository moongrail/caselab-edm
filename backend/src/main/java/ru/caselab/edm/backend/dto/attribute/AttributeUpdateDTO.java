package ru.caselab.edm.backend.dto.attribute;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "DTO for update attribute")
public class AttributeUpdateDTO {

    @Schema(description = "Attribute name", example = "attribute")
    private String name;

    @Schema(description = "Attribute data type", example = "pdf")
    private String dataType;

    @Schema(description = "Is it required attribute", example = "true")
    private Boolean required;

    @Schema(description = "DocumentType id's")
    private Set<Long> documentTypeIds;


}
