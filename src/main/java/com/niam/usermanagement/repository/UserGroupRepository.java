package com.niam.usermanagement.repository;

import com.niam.usermanagement.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    Optional<UserGroup> findByName(String name);

    boolean existsByName(String name);
}