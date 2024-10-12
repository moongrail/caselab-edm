package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AttributeDTO {

    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String dataType;

    private List<Long> documentTypeIds;

}
