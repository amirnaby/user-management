package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUserName(String token);

    Long extractUserId(String token);

    String generateToken(UserDetails userDetails);

    String generateToken(User user);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isTokenRevoked(String token);

    ResponseCookie generateJwtCookie(String jwt);

    String getJwtFromRequest(HttpServletRequest request);

    ResponseCookie getCleanJwtCookie();

    void blacklistToken(String token);
}