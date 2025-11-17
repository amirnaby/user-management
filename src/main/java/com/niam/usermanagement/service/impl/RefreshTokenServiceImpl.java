package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.exception.TokenException;
import com.niam.usermanagement.model.entities.RefreshToken;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.enums.TOKEN_TYPE;
import com.niam.usermanagement.model.payload.request.RefreshTokenRequest;
import com.niam.usermanagement.model.payload.response.RefreshTokenResponse;
import com.niam.usermanagement.model.repository.RefreshTokenRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration; // ms

    @Value("${application.security.jwt.refresh-token.cookie-name}")
    private String refreshTokenName;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        String tokenValue = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
        RefreshToken refreshToken = RefreshToken.builder()
                .revoked(false)
                .user(user)
                .token(tokenValue)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void verifyExpiration(RefreshToken token) {
        if (token == null) {
            log.error("Token is null");
            throw new TokenException(null, "Token is null");
        }
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenException(token.getToken(), "Refresh token was expired. Please authenticate again");
        }
    }

    /**
     * Rotate refresh token: oldToken -> newToken
     * If oldToken is revoked -> replay attack -> revoke all user's tokens and throw
     */
    @Transactional
    @Override
    public RefreshToken rotateRefreshToken(String oldToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new TokenException(oldToken, "Refresh token not found"));

        // check expiry
        verifyExpiration(existing);

        if (existing.isRevoked()) {
            // replay detected -> revoke all user's tokens
            log.warn("Detected refresh token replay for userId={}", existing.getUser().getId());
            revokeTokensByUser(existing.getUser());
            throw new TokenException(oldToken, "Refresh token replay detected. All sessions revoked. Please login again.");
        }

        // mark existing revoked (single-use)
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // create and return new token
        return createRefreshToken(existing.getUser().getId());
    }

    @Override
    @Transactional
    public RefreshTokenResponse generateNewToken(RefreshTokenRequest request) {
        // rotate and generate access token
        RefreshToken newRefresh = rotateRefreshToken(request.getRefreshToken());
        User user = newRefresh.getUser();

        // userDetailsService used to validate or fetch authorities (but not adding to token)
        userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = jwtService.generateToken(user);

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefresh.getToken())
                .tokenType(TOKEN_TYPE.BEARER.name())
                .build();
    }

    @Override
    public ResponseCookie generateRefreshTokenCookie(String token) {
        return ResponseCookie.from(refreshTokenName, token)
                .path("/")
                .maxAge(refreshExpiration / 1000)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .build();
    }

    @Override
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenName);
        return cookie != null ? cookie.getValue() : "";
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public void revokeTokensByUser(User user) {
        List<RefreshToken> list = refreshTokenRepository.findAllByUser(user);
        if (list != null && !list.isEmpty()) {
            refreshTokenRepository.deleteAll(list);
        }
    }

    @Override
    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenName, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
    }
}