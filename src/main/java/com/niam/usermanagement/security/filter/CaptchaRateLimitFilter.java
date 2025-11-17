package com.niam.usermanagement.security.filter;

import com.niam.usermanagement.service.RateLimitService;
import com.niam.usermanagement.utils.AuthUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CaptchaRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final AuthUtils authUtils;

    public CaptchaRateLimitFilter(RateLimitService rateLimitService, AuthUtils authUtils) {
        this.rateLimitService = rateLimitService;
        this.authUtils = authUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register"));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String ip = authUtils.extractClientIp(request);

        if (!rateLimitService.allowRequestForIp(ip)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many captcha attempts\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}