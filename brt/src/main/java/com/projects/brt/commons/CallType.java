package com.projects.brt.commons;

public enum CallType {
    OUTGOING("01"),
    INCOMING("02");

    public final String callType;

    CallType(String callType) {
        this.callType = callType;
    }

    public String value() {
        return callType;
    }
}
