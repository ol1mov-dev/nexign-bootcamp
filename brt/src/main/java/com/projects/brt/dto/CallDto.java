package com.projects.brt.dto;

import com.projects.brt.entities.User;
import lombok.Builder;

@Builder
public record CallDto(
        User user,
        String strangerMsisdn,
        String callType,
        String startTime,
        String endTime,
        Long duration
){}
