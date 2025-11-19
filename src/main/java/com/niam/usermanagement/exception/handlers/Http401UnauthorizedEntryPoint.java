package com.niam.usermanagement.exception.handlers;

import com.niam.usermanagement.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Handles 401 Unauthorized errors when the user attempts to access a protected
 * endpoint without authentication.
 */
@Component
@Slf4j
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
        log.warn("Unauthorized access at {} : {}", request.getServletPath(), ex.getMessage());
        RequestUtils.writeError(response, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}