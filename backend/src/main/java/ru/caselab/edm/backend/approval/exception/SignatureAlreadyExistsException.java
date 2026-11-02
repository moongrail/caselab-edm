package ru.caselab.edm.backend.approval.exception;

public class SignatureAlreadyExistsException extends RuntimeException {
    public SignatureAlreadyExistsException(String message) {
        super(message);
    }
}
