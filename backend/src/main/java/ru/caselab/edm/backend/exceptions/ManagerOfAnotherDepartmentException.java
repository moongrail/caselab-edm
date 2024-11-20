package ru.caselab.edm.backend.exceptions;

public class ManagerOfAnotherDepartmentException extends RuntimeException{
    public ManagerOfAnotherDepartmentException(String message) {
        super(message);
    }
}
