package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.service.PasswordExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Lazy
@RequiredArgsConstructor
@Service
public class PasswordExpirationServiceImpl implements PasswordExpirationService {
    private final UMConfigFile configFile;

    @Override
    public boolean isExpired(User user) {
        if (!configFile.isPasswordExpirationEnabled()) return false;
        if (user.isMustChangePassword() || user.getPasswordChangedAt() == null) return true;
        return user.getPasswordChangedAt().plus(configFile.getPasswordExpirationDays(), ChronoUnit.DAYS).isBefore(Instant.now());
    }

    @Override
    public void markChanged(User user) {
        user.setPasswordChangedAt(Instant.now());
    }
}
