package com.niam.usermanagement.security.filter;

import com.niam.usermanagement.security.RateLimitService;
import com.niam.usermanagement.utils.AuthUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IpRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final AuthUtils authUtils;

    public IpRateLimitFilter(RateLimitService rateLimitService, AuthUtils authUtils) {
        this.rateLimitService = rateLimitService;
        this.authUtils = authUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        // apply to auth endpoints only
        if (path.startsWith("/api/v1/auth")) {
            String ip = authUtils.extractClientIp(request);
            if (!rateLimitService.allowRequestForIp(ip)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests from IP\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}