package com.niam.usermanagement.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "um_security_audit_logs")
public class SecurityAuditLog {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String username;
    private String eventType; // LOGIN_SUCCESS, LOGIN_FAIL, OTP_SENT, etc.
    private String ip;
    private String userAgent;
    private Instant createdAt;
}