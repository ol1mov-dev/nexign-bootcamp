package com.projects.cdr.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public record CdrDto(
         Long id,
         String callType,
         String msisdn1,
         String msisdn2,
         String startTime,
         String endTime
) {}
