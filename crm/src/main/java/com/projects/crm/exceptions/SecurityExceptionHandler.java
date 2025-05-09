package com.projects.crm.exceptions;

import com.projects.crm.exceptions.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class SecurityExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .httpStatus(HttpStatus.FORBIDDEN.value())
                .message("Доступ запрещен")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}
