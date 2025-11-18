package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.repository.PermissionRepository;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.service.PermissionService;
import com.niam.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;

    @Override
    public Role createRole(String name, String desc) {
        Role r = Role.builder().name(name).description(desc).build();
        return roleRepository.save(r);
    }

    @Override
    public Role assignPermission(String roleName, String permissionCode) {
        Role r = roleRepository.findByName(roleName).orElseThrow();
        Permission p = permissionRepository.findByCode(permissionCode).orElseThrow();
        r.getPermissions().add(p);
        Role saved = roleRepository.save(r);
        permissionService.invalidateAll();
        return saved;
    }
}