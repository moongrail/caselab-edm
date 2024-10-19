package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for representing page of users")
public record UserPageDTO(
        @Schema(description = "Page number", example = "1", minimum = "0")
        int page,
        @Schema(description = "Number of elements per page", example = "10")
        int size,
        @Schema(description = "Total number of pages", example = "10")
        int totalPages,
        @Schema(description = "Total number of elements", example = "10")
        int totalElements,
        @Schema(description = "List of users")
        List<UserDTO> content
) {
}
