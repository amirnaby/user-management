package com.niam.usermanagement.service.otp;

import com.niam.usermanagement.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.otp.resend.max:3}")
    private int maxResend;

    @Value("${app.otp.resend.window.seconds:600}")
    private int windowSeconds;

    private boolean disallow(String key) {
        Deque<Instant> dq = map.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        return !AuthUtils.rateLimitHelper(dq, windowSeconds, maxResend);
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