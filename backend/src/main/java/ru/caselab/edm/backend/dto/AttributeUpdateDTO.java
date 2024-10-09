package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AttributeUpdateDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String dataType;
    private List<Long> documentTypeIds;
}
