package com.projects.hrs.controllers.requests;

import lombok.Builder;

@Builder
public record CreateAbonentHrsRequest (
        Long userId,
        Long tariffId
){ }
