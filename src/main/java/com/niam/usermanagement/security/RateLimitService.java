package com.niam.usermanagement.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimitService {

    private final Map<String, Deque<Instant>> ipMap = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> userMap = new ConcurrentHashMap<>();
    @Value("${app.security.rate-limit.window:300}")
    private int windowSeconds;
    @Value("${app.security.rate-limit.ip-max:100}")
    private int ipMax;
    @Value("${app.security.rate-limit.username-max:10}")
    private int usernameMax;

    private boolean allow(Deque<Instant> deque, int limit) {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowSeconds);

        // purge old
        while (!deque.isEmpty() && deque.peekFirst().isBefore(windowStart)) {
            deque.pollFirst();
        }

        if (deque.size() >= limit) return false;
        deque.addLast(now);
        return true;
    }

    public boolean allowRequestForIp(String ip) {
        Deque<Instant> dq = ipMap.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, ipMax);
    }

    public boolean allowRequestForUsername(String username) {
        Deque<Instant> dq = userMap.computeIfAbsent(username, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, usernameMax);
    }

    public void resetUser(String username) {
        userMap.remove(username);
    }

    public void resetIp(String ip) {
        ipMap.remove(ip);
    }
}