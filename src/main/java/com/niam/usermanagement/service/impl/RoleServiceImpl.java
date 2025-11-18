package com.niam.usermanagement.service.impl;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.repository.PermissionRepository;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserGroupRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.PermissionService;
import com.niam.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final PermissionService permissionService;

    @Override
    public Role createRole(String name, String desc) {
        Role r = Role.builder().name(name).description(desc).build();
        return roleRepository.save(r);
    }

    @Override
    @Transactional("transactionManager")
    public Role assignPermissions(String roleName, List<String> permissionCodes) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + code)))
                .toList();

        role.getPermissions().addAll(permissions);
        Role saved = roleRepository.save(role);
        permissionService.invalidateAll();
        return saved;
    }

    @Transactional("transactionManager")
    @Override
    public void deleteRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        userRepository.findAll().forEach(user -> {
            if (user.getRoles().remove(role)) {
                userRepository.save(user);
            }
        });

        userGroupRepository.findAll().forEach(group -> {
            if (group.getRoles().remove(role)) {
                userGroupRepository.save(group);
            }
        });

        permissionService.invalidateAll();
        roleRepository.delete(role);
    }
}