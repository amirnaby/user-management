package com.niam.usermanagement.otp;

import com.niam.usermanagement.entities.User;
import com.niam.usermanagement.otp.provider.OtpProvider;
import com.niam.usermanagement.otp.store.OtpStore;
import com.niam.usermanagement.repository.UserRepository;
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

    @Value("${app.otp.enabled}")
    private boolean otpEnabled;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.ttl:180}")
    private long ttlSeconds;

    @Value("${app.otp.provider}")
    private String providerName;

    public void sendLoginOtp(String username) {
        if (!otpEnabled) {
            throw new IllegalStateException("OTP is disabled");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String otp = generateOtp();

        otpStore.saveOtp(username, otp, ttlSeconds);

        OtpProvider provider = context.getBean(providerName, OtpProvider.class);

        // destination depends on provider
        String dest = providerName.contains("sms") ? user.getMobile() : user.getEmail();

        provider.sendOtp(dest, otp);
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