package ru.caselab.edm.backend.document.exception;

public class ContentTypeDetectionException extends RuntimeException {
    public ContentTypeDetectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
