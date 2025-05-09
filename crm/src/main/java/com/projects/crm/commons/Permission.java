package com.projects.crm.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Permission {


    // USER
    CAN_CREATE_USER("canCreateUser"),
    CAN_READ_USER_INFO("canReadUserInfo"),
    CAN_EDIT_USER_MSISDN("canEditUserMsisdn"),
    CAN_DELETE_USER("canDeleteUser"),

    // TARIFF
    CAN_EDIT_TARIFF("canEditTariff"),

    // BALANCE
    CAN_TOP_UP_BALANCE("canTopUpBalance"),;


    private final String permission;
}
