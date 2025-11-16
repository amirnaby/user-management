package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {
    List<DeviceSession> findAllByUserId(Long userId);

    List<DeviceSession> findAllByUserIdAndActiveTrue(Long userId);
}