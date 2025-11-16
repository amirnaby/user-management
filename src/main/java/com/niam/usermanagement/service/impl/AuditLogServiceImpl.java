package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.SecurityAuditLog;
import com.niam.usermanagement.model.repository.SecurityAuditLogRepository;
import com.niam.usermanagement.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final SecurityAuditLogRepository auditLogRepository;

    @Override
    public void log(Long userId, String username, String eventType, String ip, String userAgent) {
        auditLogRepository.save(SecurityAuditLog.builder()
                .userId(userId)
                .username(username)
                .eventType(eventType)
                .ip(ip)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build());
    }
}