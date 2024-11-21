package ru.caselab.edm.backend.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Schema(description = "DTO for creating department")
@AllArgsConstructor
public record CreateDepartmentDTO(
        @Schema(description = "Name of department", example = "Yandex GO")
        @NotBlank
        @Size(min = 2, max = 255, message = "Name length must be between {min} and {max} characters")
        String name,

        @Schema(description = "Description of department", example = "Yandex GO developers")
        String description,

        @Schema(description = "Id of parent department(if exists)", example = "0")
        Long parentId,

        @Schema(description = "ID of manager of this department")
        @NotBlank
        String manager
) {

}
