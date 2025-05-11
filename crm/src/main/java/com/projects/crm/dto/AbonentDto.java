package com.projects.crm.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AbonentDto(
        String firstName,
        String name,
        String lastName,
        String msisdn,
        BigDecimal balance
) {}
