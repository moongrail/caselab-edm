package ru.caselab.edm.backend.exceptions;

public class JwtUsernameException extends RuntimeException {
    public JwtUsernameException(String message) {
        super(message);
    }
}
