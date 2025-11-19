package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.service.AccountLockService;
import com.niam.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountLockServiceImpl implements AccountLockService {
    private final UserService userService;

    @Value("${app.security.lock.duration.seconds:900}")
    private long lockDurationSeconds;

    @Override
    public void lock(User user) {
        user.setAccountLocked(true);
        user.setLockUntil(Instant.now().plusSeconds(lockDurationSeconds));
        userService.updateUser(user);
    }

    @Override
    public void unlockIfExpired(User user) {
        if (user.isAccountLocked() && user.getLockUntil() != null && Instant.now().isAfter(user.getLockUntil())) {
            user.setAccountLocked(false);
            user.setLockUntil(null);
            userService.updateUser(user);
        }
    }

    @Override
    public void forceUnlock(String username) {
        User user = userService.loadUserByUsername(username);
        user.setAccountLocked(false);
        user.setLockUntil(null);
        userService.updateUser(user);
    }
}