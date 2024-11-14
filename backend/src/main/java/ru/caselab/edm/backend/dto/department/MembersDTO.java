package ru.caselab.edm.backend.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO for adding/deleting users to department")
public record MembersDTO(

        @Schema(description = "List of user ID's to be added/deleted to department")
        List<UUID> members
) {
}
