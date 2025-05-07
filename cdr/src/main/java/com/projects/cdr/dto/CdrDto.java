package com.projects.cdr.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
public record CdrDto(
         String callType,
         String firstMsisdn,
         String secondMsisdn,
         String startTime,
         String endTime
) {}
