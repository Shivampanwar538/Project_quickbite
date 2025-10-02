package com.quickbite.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String id, String field) {
        super(String.format("User not found with %s: %s", field, id));
    }
}