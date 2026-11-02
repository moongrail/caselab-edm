package ru.caselab.edm.backend.document.exception;

public class InvalidDocumentStateException extends RuntimeException {
    public InvalidDocumentStateException(String message) {
        super(message);
    }
}
