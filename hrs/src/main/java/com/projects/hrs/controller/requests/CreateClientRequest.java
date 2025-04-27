package com.projects.hrs.controller.requests;

public record CreateClientRequest(
        Long userId,
        Long tariffId
){}
