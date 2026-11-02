package ru.caselab.edm.security.internal;

import java.util.List;

public record InternalUserPageDTO(
        List<InternalUserDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
