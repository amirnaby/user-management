package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Permission;

import java.util.List;

public interface PermissionCacheService {
    List<Permission> getPermissionsForUser(Long userId);

    void invalidateUser(Long userId);

    void invalidateAll();
}