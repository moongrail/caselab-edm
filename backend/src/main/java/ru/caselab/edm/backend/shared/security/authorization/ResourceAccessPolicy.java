package ru.caselab.edm.backend.shared.security.authorization;

/**
 * Application-owned authorization rule for one resource type.
 *
 * <p>The security module owns the enforcement boundary and the fail-closed
 * decision contract. Domain modules contribute only the rules that require
 * domain knowledge, such as document ownership or signatory membership.</p>
 */
public interface ResourceAccessPolicy {

    /**
     * @return {@code true} when this policy is responsible for the resource type
     */
    boolean supports(String resourceType);

    /**
     * Verifies access or throws a runtime access-denied exception.
     */
    void verify(ResourceAccessRequest request);
}
