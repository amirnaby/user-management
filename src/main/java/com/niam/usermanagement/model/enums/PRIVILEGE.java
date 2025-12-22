package com.niam.usermanagement.model.enums;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public class PRIVILEGE {
    private PRIVILEGE() {
    }

    public static final String USER_READ = "USER_READ";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String CACHE_MANAGE = "CACHE_MANAGE";
    public static final String GROUP_MANAGE = "GROUP_MANAGE";
    public static final String MENU_MANAGE = "MENU_MANAGE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";
    public static final String PERMISSION_MANAGE = "PERMISSION_MANAGE";

    public static String[] values() {
        return Arrays.stream(PRIVILEGE.class.getDeclaredFields())
                .filter(field ->
                        Modifier.isStatic(field.getModifiers()) &&
                                Modifier.isFinal(field.getModifiers()) &&
                                field.getType().equals(String.class)
                )
                .map(field -> {
                    try {
                        return (String) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Cannot access privilege field: " + field.getName(), e);
                    }
                })
                .toArray(String[]::new);
    }
}