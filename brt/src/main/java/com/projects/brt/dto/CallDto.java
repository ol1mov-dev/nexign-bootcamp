package com.projects.brt.dto;

import com.projects.brt.entities.Abonent;
import lombok.Builder;
import java.time.LocalTime;

@Builder
public record CallDto(
        Abonent abonent,
        String strangerMsisdn,
        String callType,
        String startTime,
        String endTime,
        LocalTime duration
){}
