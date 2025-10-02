package com.quickbite.exception;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public MenuItemNotFoundException(String id) {
        super(String.format("Menu item not found with id: %s", id));
    }


}