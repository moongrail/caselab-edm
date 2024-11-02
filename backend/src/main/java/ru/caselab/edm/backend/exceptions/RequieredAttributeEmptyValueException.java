package ru.caselab.edm.backend.exceptions;

public class RequieredAttributeEmptyValueException extends RuntimeException{
    public RequieredAttributeEmptyValueException(String message) {
        super(message);
    }
}
