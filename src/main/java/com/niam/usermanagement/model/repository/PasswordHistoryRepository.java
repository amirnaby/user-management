package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUserIdOrderByChangedAtDesc(Long userId);
}