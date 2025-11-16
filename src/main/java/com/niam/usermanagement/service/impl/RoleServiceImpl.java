package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.repository.PermissionRepository;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.service.PermissionCacheService;
import com.niam.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionCacheService permissionCacheService;

    @Override
    public Role createRole(String name, String desc) {
        Role r = Role.builder().name(name).description(desc).build();
        return roleRepository.save(r);
    }

    @Override
    public Role assignPermission(Long roleId, Long permissionId) {
        Role r = roleRepository.findById(roleId).orElseThrow();
        Permission p = permissionRepository.findById(permissionId).orElseThrow();
        r.getPermissions().add(p);
        Role saved = roleRepository.save(r);
        permissionCacheService.invalidateAll();
        return saved;
    }
}