package com.projects.brt.dto;

public record Cdr(
        Long id,
        String callType,
        String msisdn1,
        String msisdn2,
        String startTime,
        String endTime
) { }
