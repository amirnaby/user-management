package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class InMemoryTokenBlacklistService implements TokenBlacklistService {
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();
    @Value("${app.jwt.blacklist.ttl.seconds:3600}")
    private long defaultTtlSeconds;

    @Override
    public void blacklist(String jti, long ttlSeconds) {
        if (jti == null) return;
        blacklist.put(jti, Instant.now().plusSeconds(ttlSeconds));
    }

    @Override
    public void blacklistToken(String token) {
        if (token == null) return;
        // if token is raw JWT, extract jti and blacklist with calculated TTL
        try {
            Claims claims = Jwts.parserBuilder()
                    // signing key must be injected or use JwtService helper
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String jti = claims.getId();
            Date exp = claims.getExpiration();
            long ttl = (exp.getTime() - System.currentTimeMillis()) / 1000;
            if (jti != null && ttl > 0) {
                blacklist(jti, ttl);
                return;
            }
        } catch (Exception ignored) {
        }
        // fallback: blacklist token string itself with default ttl
        blacklist.put(token, Instant.now().plusSeconds(defaultTtlSeconds));
    }

    @Override
    public boolean isBlacklisted(String jtiOrToken) {
        if (jtiOrToken == null) return false;
        Instant until = blacklist.get(jtiOrToken);
        if (until == null) return false;
        if (Instant.now().isAfter(until)) {
            blacklist.remove(jtiOrToken);
            return false;
        }
        return true;
    }
}