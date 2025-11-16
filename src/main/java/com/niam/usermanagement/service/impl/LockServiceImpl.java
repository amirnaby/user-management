package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.LockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LockServiceImpl implements LockService {
    private final UserRepository userRepository;

    @Override
    public void lockUser(User user, long lockSeconds) {
        user.setAccountLocked(true);
        user.setLockUntil(Instant.now().plusSeconds(lockSeconds));
        userRepository.save(user);
    }

    @Override
    public void unlockIfExpired(User user) {
        if (user.isAccountLocked() && user.getLockUntil() != null && Instant.now().isAfter(user.getLockUntil())) {
            user.setAccountLocked(false);
            user.setLockUntil(null);
            userRepository.save(user);
        }
    }
}
