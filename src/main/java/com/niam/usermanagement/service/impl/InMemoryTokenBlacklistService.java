package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTokenBlacklistService implements TokenBlacklistService {
    private final Map<String, Entry> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String jti, long expirySeconds) {
        if (jti == null) return;
        blacklist.put(jti, new Entry(Instant.now().plusSeconds(expirySeconds)));
    }

    @Override
    public void blacklistToken(String token) {
        blacklist.put(token, new Entry(Instant.now().plusSeconds(3600)));
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Entry e = blacklist.get(jti);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expiresAt)) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }

    private record Entry(Instant expiresAt) {
    }
}