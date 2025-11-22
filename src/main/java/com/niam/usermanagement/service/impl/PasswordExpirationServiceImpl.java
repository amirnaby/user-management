package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.service.PasswordExpirationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Lazy
@Service
public class PasswordExpirationServiceImpl implements PasswordExpirationService {
    @Value("${app.password.expiration.enabled:false}")
    private boolean enabled;

    @Value("${app.password.expiration.days:90}")
    private int days;

    @Override
    public boolean isExpired(User user) {
        if (!enabled) return false;
        if (user.isMustChangePassword() || user.getPasswordChangedAt() == null) return true;
        return user.getPasswordChangedAt().plus(days, ChronoUnit.DAYS).isBefore(Instant.now());
    }

    @Override
    public void markChanged(User user) {
        user.setPasswordChangedAt(Instant.now());
    }
}
