package com.projects.crm.controllers.requests;

import lombok.Builder;

@Builder
public record CreateAbonentHrsRequest(
        Long userId,
        Long tariffId
){ }
