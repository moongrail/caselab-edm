package ru.caselab.edm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserDTO(@NotBlank String login,
                            @NotBlank @Email String email,
                            @NotBlank String firstName,
                            @NotBlank String lastName,
                            String patronymic) {
}
