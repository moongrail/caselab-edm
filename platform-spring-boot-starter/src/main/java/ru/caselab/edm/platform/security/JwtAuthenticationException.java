package ru.caselab.edm.platform.security;

/**
 * Raised when an access token is missing required claims, malformed,
 * incorrectly signed or expired.
 */
public class JwtAuthenticationException extends RuntimeException {

    public JwtAuthenticationException(String message) {
        super(message);
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
