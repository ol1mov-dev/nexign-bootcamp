package com.projects.hrs.controller.responses;

import lombok.Builder;

@Builder
public record UserCreatedResponse(
        String tariffName
) {}
