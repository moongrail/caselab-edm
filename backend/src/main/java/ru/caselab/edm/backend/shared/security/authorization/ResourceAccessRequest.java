package ru.caselab.edm.backend.shared.security.authorization;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable request for a resource-level authorization decision.
 *
 * @param resourceType logical resource type, for example {@code document}
 * @param resourceId resource identifier or storage key
 * @param userId authenticated user identifier
 * @param action requested action
 */
public record ResourceAccessRequest(String resourceType,
                                    String resourceId,
                                    UUID userId,
                                    ResourceAction action) {

    public ResourceAccessRequest {
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(action, "action must not be null");
    }
}
