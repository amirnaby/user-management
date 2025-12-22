package com.niam.usermanagement.service.impl;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserGroupRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.PermissionService;
import com.niam.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final PermissionService permissionService;

    @Override
    public Role createRole(Role role) {
        Set<Permission> permissions = role.getPermissions().stream()
                .map(Permission::getCode)
                .map(permissionService::getByCode)
                .collect(Collectors.toSet());
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Override
    @Transactional("transactionManager")
    public Role updateRole(Role role) {
        Role existingRole = getByName(role.getName());

        Set<Permission> permissions = role.getPermissions().stream()
                .map(Permission::getCode)
                .map(permissionService::getByCode)
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        role.setDescription(existingRole.getDescription());
        Role saved = roleRepository.save(role);
        permissionService.invalidateAll();
        return saved;
    }

    @Transactional("transactionManager")
    @Override
    public void deleteRole(String roleName) {
        Role role = getByName(roleName);

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

    @Override
    public Role getByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + name));
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}