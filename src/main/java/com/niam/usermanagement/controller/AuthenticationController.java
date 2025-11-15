package com.niam.usermanagement.controller;

import com.niam.usermanagement.captcha.CaptchaProvider;
import com.niam.usermanagement.captcha.CaptchaRegistry;
import com.niam.usermanagement.captcha.CaptchaResponse;
import com.niam.usermanagement.entities.RefreshToken;
import com.niam.usermanagement.entities.User;
import com.niam.usermanagement.enums.TokenType;
import com.niam.usermanagement.otp.OtpService;
import com.niam.usermanagement.payload.request.*;
import com.niam.usermanagement.payload.response.AuthenticationResponse;
import com.niam.usermanagement.payload.response.RefreshTokenResponse;
import com.niam.usermanagement.repository.UserRepository;
import com.niam.usermanagement.service.AuthenticationService;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Authentication", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final CaptchaRegistry captchaRegistry;
    private final OtpService otpService;
    private final UserRepository userRepository;

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;

    @Value("${app.captcha.provider:localCaptchaProvider}")
    private String captchaProviderName;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Captcha is validated by CaptchaValidationFilter when enabled.
        AuthenticationResponse authenticationResponse = authenticationService.register(request);
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(authenticationResponse.getAccessToken());
        ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(authenticationResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(authenticationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        // Captcha validated by filter if enabled; OTP handling will be here later
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(authenticationResponse.getAccessToken());
        ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(authenticationResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(authenticationResponse);
    }

    @GetMapping(value = "/captcha", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        if (!captchaEnabled) {
            return ResponseEntity.badRequest().build();
        }
        CaptchaProvider provider = captchaRegistry.get(captchaProviderName);
        if (provider == null) return ResponseEntity.internalServerError().build();
        CaptchaResponse resp = provider.generate();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.generateNewToken(request));
    }

    @PostMapping("/refresh-token-cookie")
    public ResponseEntity<Void> refreshTokenCookie(HttpServletRequest request) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        var refreshTokenResponse = refreshTokenService.generateNewToken(new RefreshTokenRequest(refreshToken));
        ResponseCookie NewJwtCookie = jwtService.generateJwtCookie(refreshTokenResponse.getAccessToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, NewJwtCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }
        ResponseCookie jwtCookie = jwtService.getCleanJwtCookie();
        ResponseCookie refreshTokenCookie = refreshTokenService.getCleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String username) {
        otpService.sendLoginOtp(username);
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/login-otp")
    public ResponseEntity<AuthenticationResponse> loginOtp(@RequestParam String username,
                                                           @RequestParam String otp) {
        if (!otpService.verifyOtp(username, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String jwt = jwtService.generateToken(user);
        RefreshToken refresh = refreshTokenService.createRefreshToken(user.getId());

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(jwt);
        ResponseCookie refreshCookie = refreshTokenService.generateRefreshTokenCookie(refresh.getToken());

        var roles = user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();

        AuthenticationResponse resp = AuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refresh.getToken())
                .username(user.getUsername())
                .id(user.getId())
                .roles(roles)
                .tokenType(TokenType.BEARER.name())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(resp);
    }
}