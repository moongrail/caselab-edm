package ru.caselab.edm.backend.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.caselab.edm.backend.entity.User;

import java.util.List;
import java.util.Set;

@Schema(description = "DTO for representing department")
public record DepartmentDTO(

        @Schema(description = "ID", example = "1")
        Long id,
        @Schema(description = "Name of department", example = "Yandex GO")
        String name,
        @Schema(description = "Description of department", example = "Yandex GO developers")
        String description,
        @Schema(description = "Id of parent department(if exists)", example = "0")
        Long parentId
) {

}
