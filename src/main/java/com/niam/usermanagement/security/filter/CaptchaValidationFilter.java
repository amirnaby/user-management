package com.niam.usermanagement.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.enums.CaptchaProviderType;
import com.niam.usermanagement.model.payload.request.CaptchaValidateRequest;
import com.niam.usermanagement.service.captcha.provider.CaptchaProvider;
import com.niam.usermanagement.service.captcha.provider.CaptchaProviderRegistry;
import com.niam.usermanagement.utils.RequestUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
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
@RequiredArgsConstructor
public class CaptchaValidationFilter extends OncePerRequestFilter {
    private final CaptchaProviderRegistry captchaProviderRegistry;
    private final UMConfigFile configFile;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CaptchaProvider captchaProvider;

    @PostConstruct
    public void init() {
        captchaProvider = captchaProviderRegistry.get(CaptchaProviderType.valueOf(configFile.getCaptchaProviderName().toUpperCase()));
        if (captchaProvider == null) {
            throw new IllegalStateException("CaptchaProvider not found for name: " + configFile.getCaptchaProviderName());
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!configFile.isCaptchaEnabled()) return true;
        String path = request.getRequestURI();
        return !(path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(request.getContentType())) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Object> body;
        try {
            body = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {
            });
        } catch (Exception ex) {
            RequestUtils.writeError(response, "Invalid JSON body", HttpStatus.BAD_REQUEST);
            return;
        }

        Object tokenObj = body.get("captchaToken");
        Object respObj = body.get("captchaResponse");
        String token = tokenObj == null ? null : tokenObj.toString();
        String resp = respObj == null ? null : respObj.toString();

        if (token == null || resp == null) {
            RequestUtils.writeError(response, "captchaToken and captchaResponse are required", HttpStatus.BAD_REQUEST);
            return;
        }

        boolean valid;
        try {
            valid = captchaProvider.validate(new CaptchaValidateRequest(token, resp));
        } catch (Exception ex) {
            RequestUtils.writeError(response, "Captcha validation failed", HttpStatus.NOT_ACCEPTABLE);
            return;
        }

        if (!valid) {
            RequestUtils.writeError(response, "Invalid captcha", HttpStatus.UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}