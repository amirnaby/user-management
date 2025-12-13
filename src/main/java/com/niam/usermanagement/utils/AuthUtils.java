package com.niam.usermanagement.utils;

import com.niam.usermanagement.exception.AuthenticationException;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    private final UserRepository userRepository;
    private final UserService userService;

    public static boolean rateLimitHelper(Deque<Instant> dq, int windowSeconds, int maxResend) {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowSeconds);

        while (!dq.isEmpty() && dq.peekFirst().isBefore(windowStart)) {
            dq.pollFirst();
        }

        if (dq.size() >= maxResend) return false;

        dq.addLast(now);
        return true;
    }

    public User getCurrentUser() {
        String username = userService.getCurrentUser().getUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Logged in user not found"));
    }

    public String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}