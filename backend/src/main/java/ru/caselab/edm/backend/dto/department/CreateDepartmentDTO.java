package ru.caselab.edm.backend.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO for creating department")
public record CreateDepartmentDTO(
        @Schema(description = "Name of department", example = "Yandex GO")
        @NotBlank
        String name,
        @Schema(description = "Description of department", example = "Yandex GO developers")
        String description,
        @Schema(description = "Id of parent department(if exists)", example = "0")
        Long parentId,
        @Schema(description = "Manager of this department")
        @NotBlank
        UUID manager
) {

}
