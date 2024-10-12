package ru.caselab.edm.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for login")
public record LoginUserDTO(

        @Schema(description = "login", example = "login")
        @NotBlank String login,

        @Schema(description = "password", example = "password")
        @NotBlank String password) {

}
