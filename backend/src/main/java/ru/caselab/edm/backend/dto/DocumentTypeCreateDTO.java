package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DocumentTypeCreateDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private List<Long> attributesDocumentTypeId;
}
