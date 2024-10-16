package ru.caselab.edm.backend.exceptions;

public class ContentTypeDetectionException extends RuntimeException {
    public ContentTypeDetectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
