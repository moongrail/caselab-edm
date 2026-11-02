package ru.caselab.edm.platform.security;

import java.util.Set;
import java.util.UUID;

/**
 * Identity claims carried by an access token.
 *
 * @param userId             stable account identifier
 * @param username           username used in the token subject
 * @param roles              role names without the {@code ROLE_} prefix
 * @param departmentId       primary department, if any
 * @param managedDepartmentId department managed by the user, if any
 */
public record JwtClaims(
        UUID userId,
        String username,
        Set<String> roles,
        Long departmentId,
        Long managedDepartmentId
) {
    public JwtClaims {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
