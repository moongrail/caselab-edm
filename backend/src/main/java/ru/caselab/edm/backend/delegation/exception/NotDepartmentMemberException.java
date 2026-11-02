package ru.caselab.edm.backend.delegation.exception;

public class NotDepartmentMemberException extends RuntimeException {
    public NotDepartmentMemberException(String message) {
        super(message);
    }
}
