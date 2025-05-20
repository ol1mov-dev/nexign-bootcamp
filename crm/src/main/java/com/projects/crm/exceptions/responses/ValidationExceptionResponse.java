package com.projects.crm.exceptions.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationExceptionResponse {
    private String message;
    private Map<String, String> details;

    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
}
