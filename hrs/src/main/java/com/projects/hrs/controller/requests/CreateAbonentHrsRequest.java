package com.projects.hrs.controller.requests;

import lombok.Builder;

@Builder
public record CreateAbonentHrsRequest (
        Long userId,
        Long tariffId
){ }
