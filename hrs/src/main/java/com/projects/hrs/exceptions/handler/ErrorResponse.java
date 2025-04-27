package com.projects.hrs.exceptions.handler;

import lombok.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ErrorResponse{
    private String message;
    private int httpStatus;
    private LocalDateTime timestamp = LocalDateTime.now();
}
