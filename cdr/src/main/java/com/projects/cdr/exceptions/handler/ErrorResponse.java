package com.projects.cdr.exceptions.handler;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        String message,
        int statusCode,
        LocalDateTime timeStamp
) {}
