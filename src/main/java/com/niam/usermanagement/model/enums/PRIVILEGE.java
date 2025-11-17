package com.niam.usermanagement.model.enums;

import lombok.Getter;

@Getter
public enum PRIVILEGE {
    READ_PRIVILEGE("READ_PRIVILEGE"),
    WRITE_PRIVILEGE("WRITE_PRIVILEGE"),
    UPDATE_PRIVILEGE("UPDATE_PRIVILEGE"),
    DELETE_PRIVILEGE("DELETE_PRIVILEGE");

    private final String code;

    PRIVILEGE(String code) {
        this.code = code;
    }
}