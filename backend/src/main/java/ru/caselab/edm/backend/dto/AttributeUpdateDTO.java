package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AttributeUpdateDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String dataType;
    @NotBlank
    private boolean isRequired;

    private Set<Long> documentTypeIds;
}
