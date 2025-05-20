package com.projects.cdr.commons;

import lombok.Getter;

@Getter
public enum RuntimeExceptionMessages {
    RANDOM_USER_NOT_FOUND("Случайный пользователь не найден! База данных пуста, либо не доступна!");

    private final String message;
    RuntimeExceptionMessages(String message) {
        this.message = message;
    }
}
