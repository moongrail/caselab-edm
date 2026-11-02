package ru.caselab.edm.backend.shared.directory;

import java.util.List;

public record UserPage(
        List<UserSummary> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
}
