package com.projects.brt.dto;

import lombok.Builder;

@Builder
public record CdrDto(
        String callType,
        String firstMsisdn,
        String secondMsisdn,
        String startTime,
        String endTime
) { }
