package com.projects.crm.controllers.requests;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateUserRequest(
        @NotBlank String email,
        @NotBlank String password
) {}
