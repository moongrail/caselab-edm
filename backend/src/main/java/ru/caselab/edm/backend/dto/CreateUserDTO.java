package ru.caselab.edm.backend.dto;


public record CreateUserDTO(String login,
                            String email, String password, String firstName,
                            String lastName, String patronymic) {
}
