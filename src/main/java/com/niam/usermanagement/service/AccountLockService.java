package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final UserRepository userRepository;

    @Value("${app.security.lock.duration.seconds:900}")
    private long lockDurationSeconds;

    public void lock(User user) {
        user.setAccountLocked(true);
        user.setLockUntil(Instant.now().plusSeconds(lockDurationSeconds));
        userRepository.save(user);
    }

    public void unlockIfExpired(User user) {
        if (user.isAccountLocked() && user.getLockUntil() != null && Instant.now().isAfter(user.getLockUntil())) {
            user.setAccountLocked(false);
            user.setLockUntil(null);
            userRepository.save(user);
        }
    }

    public void forceUnlock(User user) {
        user.setAccountLocked(false);
        user.setLockUntil(null);
        userRepository.save(user);
    }
}