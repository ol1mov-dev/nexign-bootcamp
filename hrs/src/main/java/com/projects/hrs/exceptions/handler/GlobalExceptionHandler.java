package com.projects.hrs.exceptions.handler;

import com.projects.hrs.exceptions.RecordNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler({RecordNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundRecord(RecordNotFoundException ex) {
        return ResponseEntity.ok()
                .body(
                        ErrorResponse
                                .builder()
                                .message(ex.getMessage())
                                .build()
                );
    }
}
