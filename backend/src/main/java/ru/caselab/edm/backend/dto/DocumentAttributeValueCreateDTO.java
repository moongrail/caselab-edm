package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentAttributeValueCreateDTO {

    @NotNull
    private Long attributeId;
    @NotNull
    private Long documentId;
    private String value;
}
