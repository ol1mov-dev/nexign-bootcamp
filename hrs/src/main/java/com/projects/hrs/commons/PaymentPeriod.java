package com.projects.hrs.commons;

import lombok.Getter;

@Getter
public enum PaymentPeriod {
    PAY_FOR_SINGLE_CALL(0);

    public final int value;

    PaymentPeriod(int value) {
        this.value = value;
    }

}
