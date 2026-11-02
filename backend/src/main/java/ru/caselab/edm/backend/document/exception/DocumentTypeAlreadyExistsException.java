package ru.caselab.edm.backend.document.exception;

public class DocumentTypeAlreadyExistsException extends RuntimeException {
    public DocumentTypeAlreadyExistsException(String message) {
        super(message);
    }
}
