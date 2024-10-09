package ru.caselab.edm.backend.dto;

public record UpdateUserDTO(String login,
                            String email, String firstName,
                            String lastName, String patronymic) {
}
