package com.projects.brt.controllers.requests;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopUpBalanceRequest(
        @NotNull Long abonentId,
        @NotNull BigDecimal amount
) {}
