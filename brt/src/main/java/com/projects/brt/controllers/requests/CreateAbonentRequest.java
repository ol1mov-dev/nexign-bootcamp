package com.projects.brt.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateAbonentRequest(
        @NotBlank String firstName,
        @NotBlank String name,
        @NotBlank String lastName,
        @NotBlank String msisdn,
        @NotNull BigDecimal balance
) {}
