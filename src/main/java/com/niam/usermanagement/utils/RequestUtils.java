package com.niam.usermanagement.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niam.common.exception.BusinessException;
import com.niam.common.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public class RequestUtils {

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static void writeError(HttpServletResponse response, String message, HttpStatus status) {
        ErrorResponse err = ErrorResponse.builder()
                .responseCode(status.value())
                .reasonCode(status.series().value())
                .responseDescription(message)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            new ObjectMapper().writeValue(response.getOutputStream(), err);
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}