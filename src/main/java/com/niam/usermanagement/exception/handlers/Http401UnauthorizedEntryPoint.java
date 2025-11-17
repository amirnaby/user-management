package com.niam.usermanagement.exception.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niam.common.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles 401 Unauthorized errors when the user attempts to access a protected
 * endpoint without authentication.
 */
@Component
@Slf4j
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        log.warn("Unauthorized access at {} : {}", request.getServletPath(), ex.getMessage());

        ErrorResponse err = ErrorResponse.builder()
                .responseCode(HttpStatus.UNAUTHORIZED.value())
                .reasonCode(HttpStatus.UNAUTHORIZED.series().value())
                .responseDescription(ex.getMessage())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        new ObjectMapper()
                .writeValue(response.getOutputStream(), err);
    }
}