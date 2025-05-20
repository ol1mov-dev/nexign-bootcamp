package com.projects.hrs.exceptions.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BaseExceptionResponse(
        int code,
        String message,
        LocalDateTime timestamp
) {}
