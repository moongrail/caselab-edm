package ru.caselab.edm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record LoginUserDto(String login, String password) {

}
