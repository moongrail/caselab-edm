package ru.caselab.edm.security.exception;

public class ManagerOfAnotherDepartmentException extends RuntimeException{
    public ManagerOfAnotherDepartmentException(String message) {
        super(message);
    }
}
