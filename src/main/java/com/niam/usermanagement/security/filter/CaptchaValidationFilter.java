package com.niam.usermanagement.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niam.usermanagement.model.dto.CaptchaValidateRequest;
import com.niam.usermanagement.model.enums.CaptchaProviderType;
import com.niam.usermanagement.service.captcha.provider.CaptchaProvider;
import com.niam.usermanagement.service.captcha.provider.CaptchaProviderRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Validates captcha for /api/v1/auth/login and /api/v1/auth/register if captcha is enabled.
 * Expects JSON body with fields:
 * - captchaToken
 * - captchaResponse
 * Because we use CachedBodyFilter, it's safe to read the body here.
 */
@Component
public class CaptchaValidationFilter extends OncePerRequestFilter {
    private final CaptchaProviderRegistry captchaProviderRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CaptchaProvider captchaProvider;

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;

    @Value("${app.captcha.provider:LOCAL}")
    private String captchaProviderName;

    public CaptchaValidationFilter(CaptchaProviderRegistry captchaProviderRegistry) {
        this.captchaProviderRegistry = captchaProviderRegistry;
    }

    @PostConstruct
    public void init() {
        captchaProvider = captchaProviderRegistry.get(CaptchaProviderType.valueOf(captchaProviderName.toUpperCase()));
        if (captchaProvider == null) {
            throw new IllegalStateException("CaptchaProvider not found for name: " + captchaProviderName);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!captchaEnabled) return true;
        String path = request.getRequestURI();
        return !(path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(request.getContentType())) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Object> body;
        try {
            body = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {
            });
        } catch (Exception ex) {
            respond(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        Object tokenObj = body.get("captchaToken");
        Object respObj = body.get("captchaResponse");
        String token = tokenObj == null ? null : tokenObj.toString();
        String resp = respObj == null ? null : respObj.toString();

        if (token == null || resp == null) {
            respond(response, HttpServletResponse.SC_BAD_REQUEST, "captchaToken and captchaResponse are required");
            return;
        }

        boolean valid;
        try {
            valid = captchaProvider.validate(new CaptchaValidateRequest(token, resp));
        } catch (Exception ex) {
            respond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Captcha validation failed");
            return;
        }

        if (!valid) {
            respond(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid captcha");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void respond(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("status", status, "message", message));
    }
}