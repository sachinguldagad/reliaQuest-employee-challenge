package com.reliaquest.api.Exception;

public class EmployeeDataNotFoundException extends RuntimeException {
    public EmployeeDataNotFoundException(String message) {
        super(message);
    }
}
