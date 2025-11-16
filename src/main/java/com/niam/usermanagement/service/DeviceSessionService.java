package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.DeviceSession;

import java.util.List;

public interface DeviceSessionService {
    void registerLogin(Long userId, String ip, String userAgent);

    List<DeviceSession> listSessions(Long userId);

    void revokeSession(Long sessionId);

    String generateDeviceId(String ip, String ua);
}
