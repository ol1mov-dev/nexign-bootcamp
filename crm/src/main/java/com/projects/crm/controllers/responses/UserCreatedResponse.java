package com.projects.crm.controllers.responses;

import lombok.Builder;

@Builder
public record UserCreatedResponse(
        String msisdn,
        String  message
) {}
