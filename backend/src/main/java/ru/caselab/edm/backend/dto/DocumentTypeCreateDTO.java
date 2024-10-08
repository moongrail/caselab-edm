package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DocumentTypeCreateDTO {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private List<Long> attributesDocumentTypeId;
}
