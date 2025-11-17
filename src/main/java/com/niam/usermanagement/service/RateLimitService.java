package com.niam.usermanagement.service;

import java.time.Instant;
import java.util.Deque;

public interface RateLimitService {
    boolean allow(Deque<Instant> deque, int limit);

    boolean allowRequestForIp(String ip);

    boolean allowRequestForUsername(String username);

    void resetUser(String username);

    void resetIp(String ip);
}