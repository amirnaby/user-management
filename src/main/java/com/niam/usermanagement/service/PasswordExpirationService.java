package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;

public interface PasswordExpirationService {
    boolean isExpired(User user);

    void markChanged(User user);
}