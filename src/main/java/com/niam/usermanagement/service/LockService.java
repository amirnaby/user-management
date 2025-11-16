package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;

public interface LockService {
    void lockUser(User user, long lockSeconds);
    void unlockIfExpired(User user);
}