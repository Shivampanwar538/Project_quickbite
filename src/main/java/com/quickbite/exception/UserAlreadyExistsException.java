package com.quickbite.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message,cause);
    }

    public UserAlreadyExistsException(String username) {
        super(String.format("User already exists with username: %s", username));
    }
}