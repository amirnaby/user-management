package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getPermissionsForUser(Long userId);

    void invalidateUser(Long userId);

    void invalidateAll();

    Permission create(Permission dto);

    void deletePermission(String permissionCode);

    Permission getByCode(String code);

    boolean existsByCode(String code);

    List<Permission> getAll();
}