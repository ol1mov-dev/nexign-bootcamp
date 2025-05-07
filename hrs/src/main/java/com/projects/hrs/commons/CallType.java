package com.projects.hrs.commons;

import lombok.Getter;

@Getter
public enum CallType {
    OUTGOING("01"),
    INCOMING("02");

    public final String callType;

    CallType(String callType) {
        this.callType = callType;
    }
}
