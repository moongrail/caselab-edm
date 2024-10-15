package ru.caselab.edm.backend.exceptions;

public class SignatureAlreadyExistsException extends RuntimeException {
    public SignatureAlreadyExistsException(String message) {
        super(message);
    }
}
