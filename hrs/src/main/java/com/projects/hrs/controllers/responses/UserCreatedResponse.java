package com.projects.hrs.controllers.responses;

import lombok.Builder;

@Builder
public record UserCreatedResponse(
        String tariffName
) {}
