package com.niam.usermanagement.service.impl;

import com.niam.common.exception.IllegalStateException;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.entities.DeviceSession;
import com.niam.usermanagement.model.repository.DeviceSessionRepository;
import com.niam.usermanagement.service.DeviceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceSessionServiceImpl implements DeviceSessionService {
    private final DeviceSessionRepository deviceSessionRepository;
    private final UMConfigFile configFile;

    @Override
    public void registerLogin(Long userId, String ip, String userAgent) {
        String deviceId = generateDeviceId(ip, userAgent);

        deviceSessionRepository.save(DeviceSession.builder()
                .userId(userId)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .ip(ip)
                .active(true)
                .createdAt(Instant.now())
                .build());

        enforceLimit(userId);
    }

    private void enforceLimit(Long userId) {
        List<DeviceSession> sessions = deviceSessionRepository.findAllByUserIdAndActiveTrue(userId);
        if (sessions.size() <= configFile.getMaxSessions()) return;

        if ("DENY_NEW".equalsIgnoreCase(configFile.getSessionPolicy())) {
            DeviceSession newest = sessions.stream()
                    .max(Comparator.comparing(DeviceSession::getCreatedAt))
                    .orElse(null);
            if (newest != null) {
                deviceSessionRepository.delete(newest);
                throw new IllegalStateException("Maximum sessions reached");
            }
        } else {
            sessions.sort(Comparator.comparing(DeviceSession::getCreatedAt));
            int toRemove = sessions.size() - configFile.getMaxSessions();
            for (int i = 0; i < toRemove; i++) {
                DeviceSession s = sessions.get(i);
                s.setActive(false);
                deviceSessionRepository.save(s);
            }
        }
    }

    @Override
    public List<DeviceSession> listSessions(Long userId) {
        return deviceSessionRepository.findAllByUserId(userId);
    }

    @Override
    public void revokeSessionForUser(Long sessionId, Long userId) {
        deviceSessionRepository.findAllByIdAndUserIdAndActiveTrue(sessionId, userId).forEach(s -> {
            s.setActive(false);
            deviceSessionRepository.save(s);
        });
    }

    private String generateDeviceId(String ip, String userAgent) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((ip + "|" + userAgent).getBytes());
    }
}