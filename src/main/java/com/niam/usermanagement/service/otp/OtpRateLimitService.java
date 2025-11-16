package com.niam.usermanagement.service.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

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

    private boolean allow(String key) {
        Deque<Instant> dq = map.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowSeconds);

        while (!dq.isEmpty() && dq.peekFirst().isBefore(windowStart)) {
            dq.pollFirst();
        }

        if (dq.size() >= maxResend) return false;

        dq.addLast(now);
        return true;
    }

    public void checkLimitForUsername(String username) {
        if (!allow("u:" + username)) {
            throw new IllegalStateException("OTP resend limit reached for username");
        }
    }

    public void checkLimitForIp(String ip) {
        if (!allow("ip:" + ip)) {
            throw new IllegalStateException("OTP resend limit reached for IP");
        }
    }
}