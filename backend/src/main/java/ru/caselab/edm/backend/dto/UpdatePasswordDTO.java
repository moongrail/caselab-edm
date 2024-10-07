package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordDTO(@NotBlank String oldPassword,
                                @NotBlank String newPassword) {
}
