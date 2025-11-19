package com.niam.usermanagement.security.filter;

import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.TokenBlacklistService;
import com.niam.usermanagement.utils.RequestUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwt = jwtService.getJwtFromRequest(request);
        String authHeader = request.getHeader("Authorization");

        if (jwt != null && tokenBlacklistService.isBlacklisted(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        if ((jwt == null && (authHeader == null || !authHeader.startsWith("Bearer ")))) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwt == null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        String username = null;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            RequestUtils.writeError(response, "token was expired. Please authenticate again", HttpStatus.UNAUTHORIZED);
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

        filterChain.doFilter(request, response);
    }
}