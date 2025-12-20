package com.niam.usermanagement.service.otp;

import com.niam.common.exception.NotFoundException;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.enums.OtpProviderType;
import com.niam.usermanagement.model.payload.request.OtpRequest;
import com.niam.usermanagement.service.UserService;
import com.niam.usermanagement.service.otp.provider.DevOtpProvider;
import com.niam.usermanagement.service.otp.provider.OtpProvider;
import com.niam.usermanagement.service.otp.provider.OtpProviderRegistry;
import com.niam.usermanagement.service.otp.store.OtpStore;
import com.niam.usermanagement.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;

@Lazy
@Service
@RequiredArgsConstructor
public class OtpService {
    private final UserService userService;
    private final UMConfigFile configFile;
    private final OtpStore otpStore;
    private final OtpRateLimitService otpRateLimitService;
    private final OtpProviderRegistry providerRegistry;
    private final AuthUtils authUtils;

    /**
     * Send login OTP to user using configured provider.
     */
    public void sendLoginOtp(String username) {
        if (!configFile.isOtpEnabled()) throw new NotFoundException("OTP is disabled");

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        String ip = authUtils.extractClientIp(request);
        otpRateLimitService.checkLimitForIp(ip);
        otpRateLimitService.checkLimitForUsername(username);

        User user = userService.loadUserByUsername(username);
        String otp = generateOtp();
        otpStore.saveOtp(username, otp, configFile.getOtpTtlSeconds());

        OtpProvider provider = providerRegistry.get(OtpProviderType.valueOf(configFile.getOtpProviderName()));
        String dest = selectDestination(OtpProviderType.valueOf(configFile.getOtpProviderName()), user);
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
        if (!configFile.isOtpEnabled()) throw new NotFoundException("OTP is disabled");

        if (OtpProviderType.valueOf(configFile.getOtpProviderName()) == OtpProviderType.DEV) {
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
        StringBuilder sb = new StringBuilder(configFile.getOtpLength());
        for (int i = 0; i < configFile.getOtpLength(); i++) sb.append(random.nextInt(10));
        return sb.toString();
    }
}