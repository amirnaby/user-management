package com.niam.usermanagement.model.repository;

import com.niam.usermanagement.model.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);

    @Query("select distinct p from User u join u.roles r join r.permissions p where u.id = :userId")
    List<Permission> findAllByUserId(Long userId);

    boolean existsByCode(String code);
}