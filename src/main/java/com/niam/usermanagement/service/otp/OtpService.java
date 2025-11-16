package com.niam.usermanagement.service.otp;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.otp.provider.EmailOtpProvider;
import com.niam.usermanagement.service.otp.provider.OtpProvider;
import com.niam.usermanagement.service.otp.provider.SmsOtpProvider;
import com.niam.usermanagement.service.otp.store.OtpStore;
import com.niam.usermanagement.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final ApplicationContext context;
    private final AuthUtils authUtils;
    private final OtpRateLimitService rateLimit;

    @Value("${app.otp.enabled:true}")
    private boolean otpEnabled;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.ttl:180}")
    private long ttlSeconds;

    @Value("${app.otp.provider:localOtpProvider}")
    private String defaultProviderName;

    private OtpProvider resolveProvider(OTP_CHANNEL channel) {
        return switch (channel) {
            case SMS -> context.getBean("smsOtpProvider", OtpProvider.class);
            case EMAIL -> context.getBean("emailOtpProvider", OtpProvider.class);
            case LOCAL -> context.getBean("localOtpProvider", OtpProvider.class);
        };
    }

    public void sendLoginOtp(String username, HttpServletRequest request) {
        sendLoginOtp(username, request, null);
    }

    public void sendLoginOtp(String username, HttpServletRequest request, OTP_CHANNEL channel) {
        if (!otpEnabled)
            throw new IllegalStateException("OTP is disabled");

        String ip = authUtils.extractClientIp(request);

        rateLimit.checkLimitForIp(ip);
        rateLimit.checkLimitForUsername(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String otp = generateOtp();

        otpStore.saveOtp(username, otp, ttlSeconds);

        OtpProvider provider = (channel != null)
                ? resolveProvider(channel)
                : context.getBean(defaultProviderName, OtpProvider.class);

        String destination = switch (provider) {
            case SmsOtpProvider p -> user.getMobile();
            case EmailOtpProvider p -> user.getEmail();
            default -> user.getMobile();
        };

        provider.sendOtp(destination, otp);
    }

    public boolean verifyOtp(String username, String otp) {
        String correct = otpStore.getOtp(username);
        if (correct == null) return false;

        boolean ok = correct.equals(otp);
        if (ok) otpStore.removeOtp(username);

        return ok;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}