package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentTypeUpdateDTO {
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String description;
    @NotNull
    @NotEmpty
    private Long documentTypeId;

}
