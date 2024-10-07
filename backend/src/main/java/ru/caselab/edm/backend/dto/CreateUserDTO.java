package ru.caselab.edm.backend.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(@NotBlank String login,
                            @NotBlank @Email String email,
                            @NotBlank String password,
                            @NotBlank String firstName,
                            @NotBlank String lastName,
                            String patronymic) {
}
