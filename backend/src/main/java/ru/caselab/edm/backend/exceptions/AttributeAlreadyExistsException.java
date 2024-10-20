package ru.caselab.edm.backend.exceptions;

public class AttributeAlreadyExistsException extends RuntimeException {
    public AttributeAlreadyExistsException(String message) {
        super(message);
    }
}
