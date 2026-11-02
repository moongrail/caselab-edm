package ru.caselab.edm.security.exception;

/**
 * Signals that a security-module resource (user, role, department or token)
 * does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
