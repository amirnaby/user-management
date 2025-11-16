package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
}