package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DocumentTypeUpdateDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private List<Long> attributesDocumentTypeId;

}
