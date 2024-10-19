package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AttributeDTO {

    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String dataType;

    private boolean isRequired;

    private Set<Long> documentTypeIds;
}
