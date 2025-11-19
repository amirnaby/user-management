package com.niam.usermanagement.service.otp;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.common.exception.NotFoundException;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.enums.OtpProviderType;
import com.niam.usermanagement.model.payload.request.OtpRequest;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.otp.provider.DevOtpProvider;
import com.niam.usermanagement.service.otp.provider.OtpProvider;
import com.niam.usermanagement.service.otp.provider.OtpProviderRegistry;
import com.niam.usermanagement.service.otp.store.OtpStore;
import com.niam.usermanagement.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final OtpRateLimitService otpRateLimitService;
    private final OtpProviderRegistry providerRegistry;
    private final AuthUtils authUtils;

    @Value("${app.otp.enabled:true}")
    private boolean otpEnabled;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.ttl:180}")
    private long ttlSeconds;

    @Value("${app.otp.provider:DEV}")
    private OtpProviderType configuredProvider;

    /**
     * Send login OTP to user using configured provider.
     */
    public void sendLoginOtp(String username, HttpServletRequest request) {
        if (!otpEnabled) throw new NotFoundException("OTP is disabled");

        String ip = authUtils.extractClientIp(request);
        otpRateLimitService.checkLimitForIp(ip);
        otpRateLimitService.checkLimitForUsername(username);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        String otp = generateOtp();
        otpStore.saveOtp(username, otp, ttlSeconds);

        OtpProvider provider = providerRegistry.get(configuredProvider);
        String dest = selectDestination(configuredProvider, user);
        provider.send(new OtpRequest(dest, otp));
    }

    private String selectDestination(OtpProviderType p, User u) {
        return switch (p) {
            case SMS -> u.getMobile();
            case EMAIL -> u.getEmail();
            case DEV -> u.getMobile() != null && !u.getMobile().isBlank() ? u.getMobile() : u.getEmail();
        };
    }

    public boolean verifyOtp(String username, String otp) {
        if (!otpEnabled) throw new NotFoundException("OTP is disabled");

        if (configuredProvider == OtpProviderType.DEV) {
            DevOtpProvider dev = (DevOtpProvider) providerRegistry.get(OtpProviderType.DEV);
            if (dev.isMasterCode(otp)) return true;
        }

        String stored = otpStore.getOtp(username);
        if (stored == null) return false;

        boolean ok = stored.equals(otp);
        if (ok) otpStore.removeOtp(username);

        return ok;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }
}