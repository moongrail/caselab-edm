package ru.caselab.edm.backend.dto;

import java.util.UUID;

public record UserDTO(UUID id, String login,
                      String email, String firstName,
                      String lastName, String patronymic) {

}
