package ru.caselab.edm.security.internal;

import java.util.Set;
import java.util.UUID;

/**
 * Minimal user projection exposed to trusted platform services. It never
 * contains credentials.
 */
public record InternalUserDTO(
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
