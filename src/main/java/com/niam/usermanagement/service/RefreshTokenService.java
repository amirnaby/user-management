package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.RefreshToken;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.RefreshTokenRequest;
import com.niam.usermanagement.model.payload.response.RefreshTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);

    void verifyExpiration(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    @Transactional
    RefreshToken rotateRefreshToken(String oldToken);

    RefreshTokenResponse generateNewToken(RefreshTokenRequest request);

    ResponseCookie generateRefreshTokenCookie(String token);

    String getRefreshTokenFromCookies(HttpServletRequest request);

    void deleteByToken(String token);

    ResponseCookie getCleanRefreshTokenCookie();

    void revokeTokensByUser(User user);
}