package ru.caselab.edm.backend.shared.security.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

/**
 * Resolves the first policy that supports a resource type and fails closed when
 * no policy is registered. This prevents a newly introduced resource from being
 * silently exposed just because its rule was forgotten.
 */
@RequiredArgsConstructor
@Slf4j
public class ResourceAuthorizationServiceImpl implements ResourceAuthorizationService {

    private final List<ResourceAccessPolicy> policies;

    @Override
    public void verify(ResourceAccessRequest request) {
        ResourceAccessPolicy policy = policies.stream()
                .filter(candidate -> candidate.supports(request.resourceType()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No authorization policy registered for resource type '{}'",
                            safe(request.resourceType()));
                    return new AccessDeniedException(
                            "Access to resource type '%s' is denied".formatted(request.resourceType()));
                });

        policy.verify(request);
        log.debug("Access granted: type={}, id={}, action={}, user={}",
                safe(request.resourceType()), safe(request.resourceId()),
                request.action(), request.userId());
    }

    private static String safe(String value) {
        return value.replace("\r", "").replace("\n", "");
    }
}
