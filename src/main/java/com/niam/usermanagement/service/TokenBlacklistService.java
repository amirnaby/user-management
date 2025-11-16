package com.niam.usermanagement.service;

public interface TokenBlacklistService {
    void blacklist(String jti, long expirySeconds);

    void blacklistToken(String token);

    boolean isBlacklisted(String jti);
}