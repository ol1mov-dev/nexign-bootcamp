package com.projects.crm.exceptions.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        int httpStatus,
        String message,
        LocalDateTime timestamp
) { }
