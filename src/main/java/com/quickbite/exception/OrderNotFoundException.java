package com.quickbite.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderNotFoundException(String id) {
        super(String.format("Order not found with id: %s", id));
    }
}