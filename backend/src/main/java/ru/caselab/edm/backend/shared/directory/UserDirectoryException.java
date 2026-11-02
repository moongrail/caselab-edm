package ru.caselab.edm.backend.shared.directory;

public class UserDirectoryException extends RuntimeException {

    public UserDirectoryException(String message) {
        super(message);
    }

    public UserDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
