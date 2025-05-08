package com.projects.brt.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BillDto(
        Long abonentId,
        BigDecimal totalPrice
) {}
