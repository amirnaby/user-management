package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUserIdOrderByChangedAtDesc(Long userId);
}