package ru.caselab.edm.backend.dto;

import java.util.List;

public record DocumentVersionPageDto(
        int page,
        int size,
        int totalPages,
        int totalElements,
        List<DocumentVersionDTO> content
) {
}
