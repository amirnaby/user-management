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
@Table(name = "um_password_history")
public class PasswordHistory {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String oldPasswordHash;
    private Instant changedAt;
}