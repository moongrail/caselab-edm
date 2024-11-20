package ru.caselab.edm.backend.exceptions;

public class NotDepartmentMemberException extends RuntimeException {
    public NotDepartmentMemberException(String message) {
        super(message);
    }
}