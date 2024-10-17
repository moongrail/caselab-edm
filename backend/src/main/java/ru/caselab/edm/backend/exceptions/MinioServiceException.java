package ru.caselab.edm.backend.exceptions;

public class MinioServiceException extends RuntimeException {

    public MinioServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
