package ru.caselab.edm.backend.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for creating department")
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
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                message = "Manager id must be a valid UUID"
        )
        String manager
) {

}
