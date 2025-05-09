package com.projects.crm.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record RegisterUserRequest(
        @NotBlank
        @NonNull
        String firstname,

        @NotBlank
        @NonNull
        String name,

        @NotBlank
        @NonNull
        String lastname,

        @NotBlank
        @NonNull
        String email,

        @NotBlank
        @NonNull
        String password
){}
