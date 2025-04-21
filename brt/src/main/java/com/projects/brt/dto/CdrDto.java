package com.projects.brt.dto;

import lombok.Builder;

@Builder
public record CdrDto(
        Long id,
        String callType,
        String msisdn1,
        String msisdn2,
        String startTime,
        String endTime
) { }
