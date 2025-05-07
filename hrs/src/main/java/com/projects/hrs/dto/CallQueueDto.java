package com.projects.hrs.dto;

import lombok.Builder;

@Builder
public record CallQueueDto(
        Long abonentId,
        String callType,
        String callDuration
){}
