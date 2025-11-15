package com.niam.usermanagement.enums;

import lombok.Getter;

@Getter
public enum Privilege {
    READ_PRIVILEGE("READ_PRIVILEGE"),
    WRITE_PRIVILEGE("WRITE_PRIVILEGE"),
    UPDATE_PRIVILEGE("UPDATE_PRIVILEGE"),
    DELETE_PRIVILEGE("DELETE_PRIVILEGE");

    private final String code;

    Privilege(String code) {
        this.code = code;
    }
}