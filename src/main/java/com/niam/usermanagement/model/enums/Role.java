package com.niam.usermanagement.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.niam.usermanagement.model.enums.Privilege.*;

@RequiredArgsConstructor
public enum Role {
    ADMIN(
            Set.of(READ_PRIVILEGE, WRITE_PRIVILEGE, UPDATE_PRIVILEGE, DELETE_PRIVILEGE)
    ),
    USER(
            Set.of(READ_PRIVILEGE, WRITE_PRIVILEGE)
    ),
    OPERATOR(
            Set.of(READ_PRIVILEGE, WRITE_PRIVILEGE)
    );

    @Getter
    private final Set<Privilege> privileges;
}