package com.projects.brt.dto;

import lombok.Builder;

@Builder
public record CallDto(
        Long id,
        String strangerMsisdn,
        String callType,
        String startTime,
        String endTime,
        String duration
){}
