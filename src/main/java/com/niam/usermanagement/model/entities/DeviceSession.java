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

@Entity
@Table(name = "um_device_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSession {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String deviceId;
    private String userAgent;
    private String ip;
    private Instant createdAt;
    private boolean active;
}
