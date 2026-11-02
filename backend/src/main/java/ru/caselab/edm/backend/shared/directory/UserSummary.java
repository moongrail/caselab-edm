package ru.caselab.edm.backend.shared.directory;

import java.util.Set;
import java.util.UUID;

/**
 * Read-only user projection owned by the identity service. Business contexts
 * reference users by id and never persist this projection.
 */
public record UserSummary(
        UUID id,
        String login,
        String email,
        String firstName,
        String lastName,
        String patronymic,
        String position,
        Long departmentId,
        String departmentName,
        Long managedDepartmentId,
        Set<String> roles
) {
}
