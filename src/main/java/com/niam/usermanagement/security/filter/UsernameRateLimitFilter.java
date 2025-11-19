package com.niam.usermanagement.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niam.usermanagement.service.RateLimitService;
import com.niam.usermanagement.utils.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Rate limits authentication attempts for a specific username.
 * Protects login, login-otp, send-otp, and passwordless flows.
 */
@Component
public class UsernameRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UsernameRateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Apply to log in endpoints and OTP send endpoints
        return !(path.endsWith("/login") || path.endsWith("/login-otp") || path.endsWith("/send-otp") || path.contains("/passwordless"));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // For JSON body, we rely on CachedBodyFilter earlier so request.getInputStream() is readable
        String username = extractUsernameFromRequest(request);
        if (username != null && !username.isBlank()) {
            if (!rateLimitService.allowRequestForUsername(username)) {
                String message = "Too many attempts for username";
                RequestUtils.writeError(response, message, HttpStatus.TOO_MANY_REQUESTS);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        // try parameter first
        String u = request.getParameter("username");
        if (u != null && !u.isBlank()) return u;
        // then try JSON body
        try {
            Map<String, Object> body = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {
            });
            Object uname = body.get("username");
            if (uname != null) return uname.toString();
        } catch (Exception ignored) {
        }
        return null;
    }
}