package ru.caselab.edm.backend.shared.directory.api;

import java.util.List;

public record UserInfoPageDTO(
        int number,
        int size,
        int totalPages,
        int totalElements,
        List<UserInfoDTO> content
) {
}
