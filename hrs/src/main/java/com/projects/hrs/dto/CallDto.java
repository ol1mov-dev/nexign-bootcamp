package com.projects.hrs.dto;

import lombok.Builder;

@Builder
public record CallDto (
        Long abonentId,
        String callType,
        String callDuration
){}
