package com.niam.usermanagement.service;

public interface AuditLogService {
    void log(Long userId, String username, String eventType, String ip, String userAgent);
}