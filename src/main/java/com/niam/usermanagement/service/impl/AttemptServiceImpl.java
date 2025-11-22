package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.service.AttemptService;
import com.niam.usermanagement.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@RequiredArgsConstructor
@Service
public class AttemptServiceImpl implements AttemptService {
    private final UMConfigFile configFile;
    private final Map<String, Deque<Instant>> ipMap = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> userMap = new ConcurrentHashMap<>();

    private boolean allow(Deque<Instant> dq, int limit) {
        return AuthUtils.rateLimitHelper(dq, configFile.getAttemptWindowSeconds(), limit);
    }

    /**
     * Register a failed attempt for username.
     *
     * @return true if still allowed (not blocked), false if this call finds the username exceeded limit.
     */
    @Override
    public boolean registerFailureForUsername(String username) {
        Deque<Instant> dq = userMap.computeIfAbsent(username, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, configFile.getAttemptUsernameMax());
    }

    /**
     * Register a failed attempt for ip.
     *
     * @return true if still allowed (not blocked), false if this call finds the ip exceeded limit.
     */
    @Override
    public boolean registerFailureForIp(String ip) {
        Deque<Instant> dq = ipMap.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, configFile.getAttemptIpMax());
    }

    /**
     * Register success: clear counters for username and ip (or decrement policy).
     */
    @Override
    public void registerSuccess(String username, String ip) {
        if (username != null) userMap.remove(username);
        if (ip != null) ipMap.remove(ip);
    }

    @Override
    public boolean isUsernameBlocked(String username) {
        return checkBlocked(username, userMap, configFile.getAttemptUsernameMax());
    }

    @Override
    public boolean isIpBlocked(String ip) {
        return checkBlocked(ip, ipMap, configFile.getAttemptIpMax());
    }

    @Override
    public void resetUsername(String username) {
        userMap.remove(username);
    }

    @Override
    public void resetIp(String ip) {
        ipMap.remove(ip);
    }

    private boolean checkBlocked(String username, Map<String, Deque<Instant>> userMap, int usernameMax) {
        Deque<Instant> dq = userMap.get(username);
        if (dq == null) return false;
        Instant start = Instant.now().minusSeconds(configFile.getAttemptWindowSeconds());
        while (!dq.isEmpty() && dq.peekFirst().isBefore(start)) dq.pollFirst();
        return dq.size() >= usernameMax;
    }
}