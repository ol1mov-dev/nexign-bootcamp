package com.projects.cdr.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
public record CdrDto(
         Long id,
         String callType,
         String firstMsisdn,
         String secondMsisdn,
         LocalDateTime startTime,
         LocalDateTime endTime
) {}
