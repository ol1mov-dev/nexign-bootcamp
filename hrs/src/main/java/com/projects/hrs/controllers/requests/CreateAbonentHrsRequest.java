package com.projects.hrs.controllers.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateAbonentHrsRequest (
        @NotNull Long userId,
        @NotNull Long tariffId
){ }
