package com.niam.usermanagement.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niam.common.model.response.ErrorResponse;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Our jwt class extends OnePerRequestFilter to be executed on every http request
 * We can also implement the Filter interface (jakarta EE), but Spring gives us a OncePerRequestFilter
 * class that extends the GenericFilterBean, which also implements the Filter interface.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsService userDetailsService;

    /**
     * implementation is provided in config.ApplicationSecurityConfig
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        String jwt = jwtService.getJwtFromRequest(request);
        String authHeader = request.getHeader("Authorization");

        if (jwt != null && tokenBlacklistService.isBlacklisted(jwt)) {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                writeError(response, "token is blacklisted. Please unblock it first");
            }
            return;
        }

        if ((jwt == null && (authHeader == null || !authHeader.startsWith("Bearer "))) || request.getRequestURI().contains("/auth")) {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                writeError(response, "token is required. Please authenticate first");
            }
            return;
        }

        if (jwt == null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        String username = null;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            writeError(response, "token was expired. Please authenticate again");
        }
        if (StringUtils.isNotEmpty(username) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            writeError(response, "token error!");
        }
    }

    private void writeError(HttpServletResponse response, String message) {
        ErrorResponse err = ErrorResponse.builder()
                .responseCode(HttpStatus.UNAUTHORIZED.value())
                .reasonCode(HttpStatus.UNAUTHORIZED.series().value())
                .responseDescription(message)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try {
            new ObjectMapper().writeValue(response.getOutputStream(), err);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}