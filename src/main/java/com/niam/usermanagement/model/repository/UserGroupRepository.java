package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
}