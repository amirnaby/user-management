package com.niam.usermanagement.service.otp;

import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Lazy
@Service
@RequiredArgsConstructor
public class OtpRateLimitService {
    /**
     * key = "u:username" or "ip:1.2.3.4"
     */
    private final Map<String, Deque<Instant>> map = new ConcurrentHashMap<>();
    private final UMConfigFile configFile;

    private boolean disallow(String key) {
        Deque<Instant> dq = map.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        return !AuthUtils.rateLimitHelper(dq, configFile.getOtpWindowSeconds(), configFile.getOtpMaxResend());
    }

    public void checkLimitForUsername(String username) {
        if (disallow("u:" + username)) {
            throw new IllegalStateException("OTP resend limit reached for username");
        }
    }

    public void checkLimitForIp(String ip) {
        if (disallow("ip:" + ip)) {
            throw new IllegalStateException("OTP resend limit reached for IP");
        }
    }
}