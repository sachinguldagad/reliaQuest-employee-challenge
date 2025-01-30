package com.reliaquest.api.Exception;

public class EmployeeDeleteFailedException extends RuntimeException {
    public EmployeeDeleteFailedException(String message) {
        super(message);
    }

    public EmployeeDeleteFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
