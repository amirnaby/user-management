package com.niam.usermanagement.service;

import java.time.Instant;
import java.util.Deque;

public interface AttemptService {
    boolean allow(Deque<Instant> dq, int limit);

    boolean registerFailureForUsername(String username);

    boolean registerFailureForIp(String ip);

    void registerSuccess(String username, String ip);

    // helpers
    boolean isUsernameBlocked(String username);

    boolean isIpBlocked(String ip);
}