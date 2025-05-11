package com.projects.crm.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

@Builder
public record CreateUserRequest (
        @NotBlank String firstName,
        @NotBlank String name,
                  String lastName,
        @NotBlank String email,
        @NotBlank String password,
        @NonNull  Long tariffId,
        @NotBlank String msisdn,
        @NonNull  BigDecimal balance
){}
