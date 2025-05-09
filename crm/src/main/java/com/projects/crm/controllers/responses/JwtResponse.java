package com.projects.crm.controllers.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record JwtResponse(
        @NotBlank String token
) { }
