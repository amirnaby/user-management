package com.niam.usermanagement.controller;

import com.niam.common.exception.NotFoundException;
import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.annotation.StrongPassword;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.exception.AuthenticationException;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.model.payload.request.*;
import com.niam.usermanagement.model.payload.response.AuthenticationResponse;
import com.niam.usermanagement.model.payload.response.RefreshTokenResponse;
import com.niam.usermanagement.service.*;
import com.niam.usermanagement.service.captcha.CaptchaService;
import com.niam.usermanagement.service.otp.OtpService;
import com.niam.usermanagement.service.otp.provider.OtpProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

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
    private final AccountLockService accountLockService;
    private final UMConfigFile configFile;
    private final ResponseEntityUtil responseEntityUtil;

    /**
     * Register new user. Captcha validation is performed earlier in CaptchaValidationFilter.
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public ResponseEntity<ServiceResponse> register(
            @Validated({Default.class, StrongPassword.class}) @RequestBody UserDTO request) {
        AuthenticationResponse resp = authenticationService.register(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(resp.getAccessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(resp.getRefreshToken()).toString());
        return responseEntityUtil.ok(resp, headers);
    }

    /**
     * Username/password login. Captcha and rate-limit handled by filters.
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<ServiceResponse> authenticate(
            @Validated({Default.class, StrongPassword.class}) @RequestBody AuthenticationRequest request) {
        if (configFile.isOtpEnabled()) throw new NotFoundException("Use OTP login");
        return loginProcess(request);
    }

    /**
     * Fetch a new captcha challenge.
     */
    @GetMapping(value = "/captcha")
    public ResponseEntity<ServiceResponse> getCaptcha() {
        return responseEntityUtil.ok(captchaService.generate());
    }

    /**
     * Refresh token using request body.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh-token")
    public ResponseEntity<ServiceResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = refreshTokenService.generateNewToken(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(response.getAccessToken()).toString());
        return responseEntityUtil.ok(response, headers);
    }

    /**
     * Refresh token using cookie.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh-token-cookie")
    public ResponseEntity<ServiceResponse> refreshTokenCookie() {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies();
        RefreshTokenResponse response = refreshTokenService.generateNewToken(new RefreshTokenRequest(refreshToken));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(response.getAccessToken()).toString());
        return responseEntityUtil.ok(response, headers);
    }

    /**
     * Logout: blacklist access token + delete refresh token + clear cookies.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ServiceResponse> logout() {
        String accessToken = jwtService.getJwtFromRequest();
        if (accessToken != null) tokenBlacklistService.blacklistToken(accessToken);

        String refreshToken = refreshTokenService.getRefreshTokenFromCookies();
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getResponse();

        Objects.requireNonNull(response).addHeader(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString());
        Objects.requireNonNull(response).addHeader(HttpHeaders.SET_COOKIE, refreshTokenService.getCleanRefreshTokenCookie().toString());
        return responseEntityUtil.ok("Logged out");
    }

    /**
     * Change password for current user.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<ServiceResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return responseEntityUtil.ok("Password changed successfully");
    }

    /**
     * Reset another user's password (only admins).
     */
    @HasPermission(PRIVILEGE.USER_MANAGE)
    @PostMapping("/reset-password")
    public ResponseEntity<ServiceResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return responseEntityUtil.ok("Password reset successfully");
    }

    /**
     * Send OTP to user.
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/send-otp")
    public ResponseEntity<ServiceResponse> sendOtp(@RequestParam String username) {
        otpService.sendLoginOtp(username);
        return responseEntityUtil.ok("OTP sent");
    }

    /**
     * Login using OTP (2FA or passwordless).
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/login-otp")
    public ResponseEntity<ServiceResponse> loginOtp(
            @Validated({Default.class, StrongPassword.class, OtpProvider.class})
            @RequestBody AuthenticationRequest authenticationRequest) {
        if (configFile.isPasswordlessEnabled()) throw new NotFoundException("Use passwordless login");
        if (!otpService.verifyOtp(authenticationRequest.getUsername(), authenticationRequest.getOtp())) {
            throw new AuthenticationException("Invalid or expired OTP");
        }
        return loginProcess(authenticationRequest);
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/passwordless/login")
    public ResponseEntity<ServiceResponse> passwordlessLogin(
            @Validated({Default.class, OtpProvider.class}) @RequestBody AuthenticationRequest authenticationRequest) {
        if (!configFile.isPasswordlessEnabled()) throw new NotFoundException("Use OTP login");
        if (!otpService.verifyOtp(authenticationRequest.getUsername(), authenticationRequest.getOtp())) {
            throw new AuthenticationException("Invalid or expired OTP");
        }
        return loginProcess(authenticationRequest);
    }

    @PreAuthorize("hasAuthority('USER_MANAGE') and hasRole('ADMIN')")
    @PostMapping("/admin/unlock/{username}")
    public ResponseEntity<ServiceResponse> unlock(@PathVariable String username) {
        accountLockService.forceUnlock(username);
        return responseEntityUtil.ok("unlocked");
    }

    private ResponseEntity<ServiceResponse> loginProcess(AuthenticationRequest request) {
        AuthenticationResponse resp = authenticationService.authenticate(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(resp.getAccessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenService.generateRefreshTokenCookie(resp.getRefreshToken()).toString());
        return responseEntityUtil.ok(resp, headers);
    }
}