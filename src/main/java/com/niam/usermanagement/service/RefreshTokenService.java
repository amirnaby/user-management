package com.niam.usermanagement.service;

import com.niam.usermanagement.entities.RefreshToken;
import com.niam.usermanagement.entities.User;
import com.niam.usermanagement.payload.request.RefreshTokenRequest;
import com.niam.usermanagement.payload.response.RefreshTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    RefreshTokenResponse generateNewToken(RefreshTokenRequest request);

    ResponseCookie generateRefreshTokenCookie(String token);

    String getRefreshTokenFromCookies(HttpServletRequest request);

    void deleteByToken(String token);

    ResponseCookie getCleanRefreshTokenCookie();

    void revokeTokensByUser(User user);
}