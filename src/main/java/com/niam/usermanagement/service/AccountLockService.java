package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;

public interface AccountLockService {
    void lock(User user);

    void unlockIfExpired(User user);

    void forceUnlock(User user);
}