package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {
    List<DeviceSession> findAllByUserId(Long userId);

    List<DeviceSession> findAllByIdAndUserIdAndActiveTrue(Long id, Long userId);

    List<DeviceSession> findAllByUserIdAndActiveTrue(Long userId);
}