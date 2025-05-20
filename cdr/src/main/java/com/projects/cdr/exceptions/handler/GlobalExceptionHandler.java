package com.projects.cdr.exceptions.handler;

import com.projects.cdr.exceptions.RandomUserNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{
    @ExceptionHandler(RandomUserNotFound.class)
    public ResponseEntity<ErrorResponse> handleRandomUserNotFound(RandomUserNotFound ex) {
        log.warn("RandomUserNotFound: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .timeStamp(LocalDateTime.now())
                        .build());
    }
}
