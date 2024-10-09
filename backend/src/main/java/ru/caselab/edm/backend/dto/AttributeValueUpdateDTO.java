package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttributeValueUpdateDTO {
    @NotNull
    private Long attributeId;
    @NotNull
    private Long documentId;
    private String value;
}
