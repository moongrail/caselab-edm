package ru.caselab.edm.backend.shared.security.authorization;

/**
 * Central enforcement point for resource-level authorization.
 */
public interface ResourceAuthorizationService {

    /**
     * Verifies that the user may perform the requested action.
     *
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     */
    void verify(ResourceAccessRequest request);
}
