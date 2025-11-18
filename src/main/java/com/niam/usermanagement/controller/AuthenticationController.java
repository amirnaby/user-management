package com.niam.usermanagement.controller;

import com.niam.common.exception.BusinessException;
import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.RefreshToken;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.*;
import com.niam.usermanagement.model.payload.response.AuthenticationResponse;
import com.niam.usermanagement.model.payload.response.RefreshTokenResponse;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.*;
import com.niam.usermanagement.service.captcha.CaptchaService;
import com.niam.usermanagement.service.otp.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication and session-related endpoints (login, register, OTP, refresh token, logout).
 */
@Tag(name = "Authentication", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final CaptchaService captchaService;
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final AccountLockService accountLockService;
    private final ResponseEntityUtil responseEntityUtil;

    /**
     * Register new user. Captcha validation is performed earlier in CaptchaValidationFilter.
     */
    @PostMapping("/register")
    public ResponseEntity<ServiceResponse> register(@Valid @RequestBody UserDTO request) {
        AuthenticationResponse resp = authenticationService.register(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(resp.getAccessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(resp.getRefreshToken()).toString());
        return responseEntityUtil.ok(resp, headers);
    }

    /**
     * Username/password login. Captcha and rate-limit handled by filters.
     */
    @PostMapping("/login")
    public ResponseEntity<ServiceResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletRequest servletRequest) {
        AuthenticationResponse resp = authenticationService.authenticate(request, servletRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(resp.getAccessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(resp.getRefreshToken()).toString());
        return responseEntityUtil.ok(resp, headers);
    }

    /**
     * Fetch a new captcha challenge.
     */
    @GetMapping(value = "/captcha", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceResponse> getCaptcha() {
        try {
            return responseEntityUtil.ok(captchaService.generate());
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }
    }

    /**
     * Refresh token using request body.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ServiceResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return responseEntityUtil.ok(refreshTokenService.generateNewToken(request));
    }

    /**
     * Refresh token using cookie.
     */
    @PostMapping("/refresh-token-cookie")
    public ResponseEntity<ServiceResponse> refreshTokenCookie(HttpServletRequest request) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        RefreshTokenResponse response = refreshTokenService.generateNewToken(new RefreshTokenRequest(refreshToken));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(response.getAccessToken()).toString());
        return responseEntityUtil.ok(response, headers);
    }

    /**
     * Logout: blacklist access token + delete refresh token + clear cookies.
     */
    @PostMapping("/logout")
    public ResponseEntity<ServiceResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtService.getJwtFromRequest(request);
        if (accessToken != null) tokenBlacklistService.blacklistToken(accessToken);

        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenService.getCleanRefreshTokenCookie().toString());
        return responseEntityUtil.ok(Map.of("message", "Logged out"));
    }

    /**
     * Change password for current user.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ServiceResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest servletRequest) {
        authenticationService.changePassword(request, servletRequest);
        return responseEntityUtil.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Reset another user's password (only admins).
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest servletRequest) {
        authenticationService.resetPassword(request, servletRequest);
        return responseEntityUtil.ok(Map.of("message", "Password reset successfully"));
    }

    /**
     * Send OTP to user.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ServiceResponse> sendOtp(@RequestParam String username, HttpServletRequest request) {
        otpService.sendLoginOtp(username, request);
        return responseEntityUtil.ok(Map.of("message", "OTP sent"));
    }

    /**
     * Login using OTP (2FA or passwordless).
     */
    @PostMapping("/login-otp")
    public ResponseEntity<ServiceResponse> loginOtp(@RequestParam String username, @RequestParam String otp) {
        if (!otpService.verifyOtp(username, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String jwt = jwtService.generateToken(user);
        RefreshToken refresh = refreshTokenService.createRefreshToken(user.getId());
        AuthenticationResponse resp = AuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refresh.getToken())
                .id(user.getId())
                .username(user.getUsername()).roles(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream()).map(Permission::getName).toList())
                .tokenType("Bearer")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(jwt).toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(refresh.getToken()).toString());
        return responseEntityUtil.ok(resp, headers);
    }

    @ConditionalOnBooleanProperty("app.passwordless.enabled")
    @PostMapping("/passwordless/send-otp")
    public ResponseEntity<ServiceResponse> sendPasswordlessOtp(@RequestParam String username, HttpServletRequest request) {
        otpService.sendLoginOtp(username, request);
        return responseEntityUtil.ok(Map.of("message", "OTP sent"));
    }

    @ConditionalOnBooleanProperty("app.passwordless.enabled")
    @PostMapping("/passwordless/login")
    public ResponseEntity<ServiceResponse> passwordlessLogin(@RequestParam String username, @RequestParam String otp) {
        if (!otpService.verifyOtp(username, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        refreshTokenService.revokeTokensByUser(user);
        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(jwt);
        ResponseCookie refreshCookie = refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken());
        var authorities = user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).map(Permission::getName).toList();
        AuthenticationResponse resp = AuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .id(user.getId())
                .username(user.getUsername())
                .roles(authorities)
                .tokenType("Bearer")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return responseEntityUtil.ok(resp, headers);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/unlock/{userId}")
    public ResponseEntity<ServiceResponse> unlock(@PathVariable Long userId) {
        accountLockService.forceUnlock(userId);
        return responseEntityUtil.ok(Map.of("message", "unlocked"));
    }
}