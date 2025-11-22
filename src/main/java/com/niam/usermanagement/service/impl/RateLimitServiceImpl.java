package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.service.RateLimitService;
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
public class RateLimitServiceImpl implements RateLimitService {
    private final Map<String, Deque<Instant>> ipMap = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> userMap = new ConcurrentHashMap<>();
    private final UMConfigFile configFile;

    @Override
    public boolean allow(Deque<Instant> deque, int limit) {
        return AuthUtils.rateLimitHelper(deque, configFile.getRateLimitWindowSeconds(), limit);
    }

    @Override
    public boolean allowRequestForIp(String ip) {
        Deque<Instant> dq = ipMap.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, configFile.getRateLimitIpMax());
    }

    @Override
    public boolean allowRequestForUsername(String username) {
        Deque<Instant> dq = userMap.computeIfAbsent(username, k -> new ConcurrentLinkedDeque<>());
        return allow(dq, configFile.getRateLimitUsernameMax());
    }

    @Override
    public void resetUser(String username) {
        userMap.remove(username);
    }

    @Override
    public void resetIp(String ip) {
        ipMap.remove(ip);
    }
}