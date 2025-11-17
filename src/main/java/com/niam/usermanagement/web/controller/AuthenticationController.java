package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.model.dto.CaptchaResponse;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;

    /**
     * Register new user. Captcha validation is performed earlier in CaptchaValidationFilter.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse resp = authenticationService.register(request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(resp.getAccessToken()).toString()).header(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(resp.getRefreshToken()).toString()).body(resp);
    }

    /**
     * Username/password login. Captcha and rate-limit handled by filters.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletRequest servletRequest) {
        AuthenticationResponse resp = authenticationService.authenticate(request, servletRequest);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(resp.getAccessToken()).toString()).header(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(resp.getRefreshToken()).toString()).body(resp);
    }

    /**
     * Fetch a new captcha challenge.
     */
    @GetMapping(value = "/captcha", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        if (!captchaEnabled) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(captchaService.generate());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Refresh token using request body.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.generateNewToken(request));
    }

    /**
     * Refresh token using cookie.
     */
    @PostMapping("/refresh-token-cookie")
    public ResponseEntity<Void> refreshTokenCookie(HttpServletRequest request) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        RefreshTokenResponse response = refreshTokenService.generateNewToken(new RefreshTokenRequest(refreshToken));
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(response.getAccessToken()).toString()).build();
    }

    /**
     * Logout: blacklist access token + delete refresh token + clear cookies.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtService.getJwtFromRequest(request);
        if (accessToken != null) tokenBlacklistService.blacklistToken(accessToken);

        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenService.getCleanRefreshTokenCookie().toString());
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /**
     * Change password for current user.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest servletRequest) {
        authenticationService.changePassword(request, servletRequest);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Reset another user's password (only admins).
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest servletRequest) {
        authenticationService.resetPassword(request, servletRequest);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    /**
     * Send OTP to user.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String username, HttpServletRequest request) {
        otpService.sendLoginOtp(username, request);
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    /**
     * Login using OTP (2FA or passwordless).
     */
    @PostMapping("/login-otp")
    public ResponseEntity<AuthenticationResponse> loginOtp(@RequestParam String username, @RequestParam String otp) {
        if (!otpService.verifyOtp(username, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String jwt = jwtService.generateToken(user);
        RefreshToken refresh = refreshTokenService.createRefreshToken(user.getId());
        AuthenticationResponse resp = AuthenticationResponse.builder().accessToken(jwt).refreshToken(refresh.getToken()).id(user.getId()).username(user.getUsername()).roles(user.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getName).toList()).tokenType("Bearer").build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(jwt).toString()).header(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(refresh.getToken()).toString()).body(resp);
    }

    @ConditionalOnBooleanProperty("app.passwordless.enabled")
    @PostMapping("/passwordless/send-otp")
    public ResponseEntity<?> sendPasswordlessOtp(@RequestParam String username, HttpServletRequest request) {
        otpService.sendLoginOtp(username, request);
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @ConditionalOnBooleanProperty("app.passwordless.enabled")
    @PostMapping("/passwordless/login")
    public ResponseEntity<AuthenticationResponse> passwordlessLogin(@RequestParam String username, @RequestParam String otp) {
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
        AuthenticationResponse resp = AuthenticationResponse.builder().accessToken(jwt).refreshToken(refreshToken.getToken()).id(user.getId()).username(user.getUsername()).roles(authorities).tokenType("Bearer").build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).body(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/unlock/{userId}")
    public ResponseEntity<?> unlock(@PathVariable Long userId) {
        accountLockService.forceUnlock(userId);
        return ResponseEntity.ok(Map.of("message", "unlocked"));
    }
}