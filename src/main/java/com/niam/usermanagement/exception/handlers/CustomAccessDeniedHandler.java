package com.niam.usermanagement.exception.handlers;

import com.niam.usermanagement.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Handles 403 Forbidden errors thrown by Spring Security when authenticated
 * users try to access resources they are not authorized for.
 */
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {
        log.warn("Access denied at {} : {}", request.getServletPath(), ex.getMessage());
        RequestUtils.writeError(response, ex.getMessage(), HttpStatus.FORBIDDEN);
    }
}