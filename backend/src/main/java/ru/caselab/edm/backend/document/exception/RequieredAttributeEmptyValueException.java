package ru.caselab.edm.backend.document.exception;

public class RequieredAttributeEmptyValueException extends RuntimeException {
    public RequieredAttributeEmptyValueException(String message) {
        super(message);
    }
}
