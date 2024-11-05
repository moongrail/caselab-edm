package ru.caselab.edm.backend.dto.attributevalue;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentAttributeValueDTO {

    @NotNull
    private Long attributeId;

    private String value;
}
