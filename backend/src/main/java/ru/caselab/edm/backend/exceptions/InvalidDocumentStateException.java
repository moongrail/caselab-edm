package ru.caselab.edm.backend.exceptions;

public class InvalidDocumentStateException extends RuntimeException {
    public InvalidDocumentStateException(String message) {
        super(message);
    }
}
