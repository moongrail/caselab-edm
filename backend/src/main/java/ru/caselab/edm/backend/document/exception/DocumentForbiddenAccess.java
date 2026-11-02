package ru.caselab.edm.backend.document.exception;

public class DocumentForbiddenAccess extends RuntimeException {
    public DocumentForbiddenAccess(String message) {
        super(message);
    }
}
