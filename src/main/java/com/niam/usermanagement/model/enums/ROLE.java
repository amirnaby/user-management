package com.niam.usermanagement.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public enum ROLE {
    ADMIN(
            Set.of(PRIVILEGE.values())
    ),
    USER(
            Set.of()
    );

    @Getter
    private final Set<String> PRIVILEGES;
}