package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final TokenBlacklistService tokenBlacklistService;
    private final UMConfigFile configFile;

    /* ---------------------- Extract Claims ---------------------- */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object val = claims.get("uid");
        if (val == null) return null;
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* ---------------------- Token Generation ---------------------- */
    @Override
    public String generateToken(User user) {
        return generateToken(user.getUsername(), user.getId());
    }

    private String generateToken(String username, Long userId) {
        long now = System.currentTimeMillis();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(username)
                .setId(jti)                 // JTI for blacklist
                .claim("uid", userId)       // userId safe bind
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + configFile.getJwtExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ---------------------- Validity ---------------------- */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null) return false;

        String username = extractUsername(token);
        if (username == null || !username.equals(userDetails.getUsername()))
            return false;

        if (isTokenRevoked(token))
            return false;

        return !isTokenExpired(token);
    }

    @Override
    public boolean isTokenRevoked(String token) {
        try {
            String jti = extractAllClaims(token).getId();
            if (jti == null) return false;
            return tokenBlacklistService.isBlacklisted(jti);
        } catch (JwtException e) {
            return true;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /* ---------------------- Cookie Helpers ---------------------- */
    @Override
    public ResponseCookie generateJwtCookie(String jwt) {
        return ResponseCookie.from(configFile.getJwtCookieName(), jwt)
                .httpOnly(true)
                .secure(!"dev".equals(configFile.getActiveProfile()))
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(configFile.getJwtExpiration()).getSeconds())
                .build();
    }

    @Override
    public String getJwtFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        // 1. try cookie
        Cookie cookie = WebUtils.getCookie(request, configFile.getJwtCookieName());
        if (cookie != null && !cookie.getValue().isBlank())
            return cookie.getValue();

        // 2. try header
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer "))
            return auth.substring(7);

        return null;
    }

    @Override
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(configFile.getJwtCookieName(), "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
    }

    /* ---------------------- Claim Utilities ---------------------- */
    private <T> T extractClaim(String token, Function<Claims, T> fn) {
        return fn.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(configFile.getSecretKey()));
    }

    /* ---------------------- Blacklist ---------------------- */
    @Override
    public void blacklistToken(String token) {
        if (token == null) return;

        try {
            Claims claims = extractAllClaims(token);
            String jti = claims.getId();
            Date exp = claims.getExpiration();

            long ttl = (exp.getTime() - System.currentTimeMillis()) / 1000;
            if (jti != null && ttl > 0) {
                tokenBlacklistService.blacklist(jti, ttl);
            }

        } catch (JwtException ignored) {
        }
    }
}