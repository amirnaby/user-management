package com.niam.usermanagement.service.impl;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.repository.PermissionRepository;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple permission cache keyed by userId. TTL-based.
 * Suitable for single-instance or dev. Replace with Redis/Caffeine if needed.
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final Map<Long, Entry> cache = new ConcurrentHashMap<>();
    private final UMConfigFile configFile;

    private record Entry(List<Permission> perms, Instant expiresAt) {}

    @Override
    public List<Permission> getPermissionsForUser(Long userId) {
        Entry e = cache.get(userId);
        if (e != null && Instant.now().isBefore(e.expiresAt)) {
            return e.perms;
        }
        List<Permission> perms = permissionRepository.findAllByUserId(userId);
        cache.put(userId, new Entry(perms, Instant.now().plusSeconds(configFile.getPermissionTtlSeconds())));
        return perms;
    }

    @Override
    public void invalidateUser(Long userId) {
        cache.remove(userId);
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public Permission create(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Transactional("transactionManager")
    @Override
    public void deletePermission(String permissionCode) {
        Permission permission = getByCode(permissionCode);

        roleRepository.findAll().forEach(role -> {
            if (role.getPermissions().remove(permission)) {
                roleRepository.save(role);
            }
        });

        invalidateAll();
        permissionRepository.delete(permission);
    }

    @Override
    public Permission getByCode(String code) {
        return permissionRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + code));
    }

    @Override
    public boolean existsByCode(String code) {
        return permissionRepository.existsByCode(code);
    }

    @Override
    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }
}