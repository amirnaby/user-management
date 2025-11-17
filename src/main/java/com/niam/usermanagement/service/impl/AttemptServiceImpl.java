package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.service.AttemptService;
import com.niam.usermanagement.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AttemptServiceImpl implements AttemptService {
    private final Map<String, Deque<Instant>> usernameMap = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> ipMap = new ConcurrentHashMap<>();
    @Value("${app.security.attempt.window.seconds:300}")
    private int windowSeconds;
    @Value("${app.security.attempt.username.max:5}")
    private int usernameMax;
    @Value("${app.security.attempt.ip.max:50}")
    private int ipMax;

    @Override
    public boolean registerFailureForUsername(String username) {
        Deque<Instant> dq = usernameMap.computeIfAbsent(username, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, usernameMax);
    }

    @Override
    public boolean registerFailureForIp(String ip) {
        Deque<Instant> dq = ipMap.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, ipMax);
    }

    @Override
    public void registerSuccess(String username, String ip) {
        usernameMap.remove(username);
        ipMap.remove(ip);
    }

    @Override
    public boolean isUsernameBlocked(String username) {
        return checkBlocked(username, usernameMap, usernameMax);
    }

    @Override
    public boolean isIpBlocked(String ip) {
        return checkBlocked(ip, ipMap, ipMax);
    }

    private boolean allow(Deque<Instant> dq, int limit) {
        return AuthUtils.rateLimitHelper(dq, windowSeconds, limit);
    }

    private boolean checkBlocked(String username, Map<String, Deque<Instant>> usernameMap, int usernameMax) {
        Deque<Instant> dq = usernameMap.get(username);
        if (dq == null) return false;
        Instant now = Instant.now();
        Instant start = now.minusSeconds(windowSeconds);
        while (!dq.isEmpty() && dq.peekFirst().isBefore(start)) dq.pollFirst();
        return dq.size() >= usernameMax;
    }
}