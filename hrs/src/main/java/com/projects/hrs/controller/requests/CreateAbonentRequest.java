package com.projects.hrs.controller.requests;

public record CreateAbonentRequest(
        Long userId,
        Long tariffId
) { }
