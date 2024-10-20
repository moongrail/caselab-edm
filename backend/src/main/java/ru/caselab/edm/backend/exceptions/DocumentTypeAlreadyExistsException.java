package ru.caselab.edm.backend.exceptions;

public class DocumentTypeAlreadyExistsException extends RuntimeException {
    public DocumentTypeAlreadyExistsException(String message) {
        super(message);
    }
}
