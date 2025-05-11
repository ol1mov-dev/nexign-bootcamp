package com.projects.hrs.controllers.requests;

public record CreateAbonentRequest(
        Long userId,
        Long tariffId
) { }
