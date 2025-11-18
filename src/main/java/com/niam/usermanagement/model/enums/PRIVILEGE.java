package com.niam.usermanagement.model.enums;

import lombok.Getter;

@Getter
public enum PRIVILEGE {
    USER_MANAGE("USER_MANAGE"),
    USER_READ("USER_READ");

    private final String code;

    PRIVILEGE(String code) {
        this.code = code;
    }
}