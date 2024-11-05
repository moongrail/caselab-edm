package ru.caselab.edm.backend.dto.attribute;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class AttributeUpdateDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String dataType;

    private boolean isRequired;

    private Set<Long> documentTypeIds;
}
