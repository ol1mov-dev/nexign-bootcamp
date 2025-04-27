package com.projects.brt.dto;

import lombok.Builder;

@Builder
public record CallQueueDto(
        Long userId,
        Long callDurationInSeconds
){}
