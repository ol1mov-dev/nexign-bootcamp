package com.projects.brt.dto;

import lombok.Builder;

@Builder
public record CallQueueDto(
        Long abonentId,
        String callType,
        String callDuration
){}
