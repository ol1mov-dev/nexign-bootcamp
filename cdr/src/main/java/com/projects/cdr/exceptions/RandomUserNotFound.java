package com.projects.cdr.exceptions;

public class RandomUserNotFound extends RuntimeException {
    public RandomUserNotFound(String message) {
        super(message);
    }
}
