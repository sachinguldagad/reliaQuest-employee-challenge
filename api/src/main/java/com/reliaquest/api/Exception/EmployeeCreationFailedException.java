package com.reliaquest.api.Exception;

public class EmployeeCreationFailedException extends RuntimeException {
    public EmployeeCreationFailedException(String message) {
        super(message);
    }
}
