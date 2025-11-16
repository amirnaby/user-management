package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.RefreshToken;
import com.niam.usermanagement.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(User user);
}