package ru.caselab.edm.backend.exceptions;

public class DocumentForbiddenAccess extends RuntimeException {
    public DocumentForbiddenAccess(String message) {
        super(message);
    }
}
