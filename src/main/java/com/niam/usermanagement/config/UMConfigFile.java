package com.niam.usermanagement.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class UMConfigFile {
    @Value("${spring.profiles.active:sec}")
    private String activeProfile;

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;
    @Value("${app.captcha.provider:LOCAL}")
    private String captchaProviderName;
    @Value("${captcha.local.image.width:200}")
    private String captchaImgWidth;
    @Value("${captcha.local.image.height:50}")
    private String captchaImgHeight;
    @Value("${captcha.local.length:6}")
    private String captchaCharLength;
    @Value("${captcha.ttl.seconds:120}")
    private int captchaTtlSeconds;

    @Value("${app.otp.enabled:false}")
    private boolean isOtpEnabled;
    @Value("${app.passwordless.enabled:false}")
    private boolean isPasswordlessEnabled;
    @Value("${app.otp.provider:DEV}")
    private String otpProviderName;
    @Value("${app.otp.length:6}")
    private int otpLength;
    @Value("${app.otp.ttl:180}")
    private long otpTtlSeconds;
    @Value("${app.otp.resend.max:3}")
    private int otpMaxResend;
    @Value("${app.otp.resend.window.seconds:600}")
    private int otpWindowSeconds;
    @Value("${app.otp.dev.master-code:999999}")
    private String otpDevMasterCode;

    @Value("${app.security.rate-limit.window:300}")
    private int rateLimitWindowSeconds;
    @Value("${app.security.rate-limit.ip-max:100}")
    private int rateLimitIpMax;
    @Value("${app.security.rate-limit.username-max:10}")
    private int rateLimitUsernameMax;

    @Value("${app.security.attempt.window.seconds:300}")
    private int attemptWindowSeconds;
    @Value("${app.security.attempt.username.max:5}")
    private int attemptUsernameMax;
    @Value("${app.security.attempt.ip.max:50}")
    private int attemptIpMax;
    @Value("${app.security.lock.duration.seconds:900}")
    private long lockDurationSeconds;
    @Value("${app.jwt.blacklist.ttl.seconds:3600}")
    private long blacklistTtlSeconds;

    @Value("${app.security.max.sessions.per.user:3}")
    private int maxSessions;
    @Value("${app.security.session.policy:KICK_OLDEST}")
    private String sessionPolicy;

    @Value("${app.password.expiration.enabled:false}")
    private boolean passwordExpirationEnabled;
    @Value("${app.password.expiration.days:90}")
    private int passwordExpirationDays;

    @Value("${application.security.jwt.secret-key:586B633834416E396D7436753879382F423F4428482B4C6250655367566B5970}")
    private String secretKey;
    @Value("${application.security.jwt.expiration:900000}") // default 15 minutes
    private long jwtExpiration;
    @Value("${application.security.jwt.cookie-name:access-token}")
    private String jwtCookieName;
    @Value("${application.security.jwt.refresh-token.expiration:1296000000}")
    private long refreshExpiration;
    @Value("${application.security.jwt.refresh-token.cookie-name:refresh-token}")
    private String refreshTokenName;

    @Value("${app.permission.cache.ttl.seconds:300}")
    private long permissionTtlSeconds;
}