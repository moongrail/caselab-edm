package ru.caselab.edm.security.config;

import org.springframework.stereotype.Component;
import ru.caselab.edm.platform.security.JwtClaims;
import ru.caselab.edm.security.domain.Role;
import ru.caselab.edm.security.domain.User;
import ru.caselab.edm.security.domain.UserInfoDetails;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts an identity aggregate into the claims shared by all services.
 */
@Component
public class JwtClaimsFactory {

    public JwtClaims from(User user) {
        Set<String> roles = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        return new JwtClaims(
                user.getId(),
                user.getLogin(),
                roles,
                user.getDepartment() == null ? null : user.getDepartment().getId(),
                user.getLeadDepartment() == null ? null : user.getLeadDepartment().getId()
        );
    }

    public JwtClaims from(UserInfoDetails user) {
        return new JwtClaims(
                user.getId(),
                user.getUsername(),
                user.getRoleNames(),
                user.getDepartmentId(),
                user.getManagedDepartmentId()
        );
    }
}
