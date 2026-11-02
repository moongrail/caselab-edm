package ru.caselab.edm.backend.document.exception;

public class AttributeAlreadyExistsException extends RuntimeException {
    public AttributeAlreadyExistsException(String message) {
        super(message);
    }
}
