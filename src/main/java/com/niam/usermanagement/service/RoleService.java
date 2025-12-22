package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Role;

import java.util.List;

public interface RoleService {
    Role createRole(Role role);

    Role updateRole(Role role);

    void deleteRole(String roleName);

    Role getByName(String name);

    List<Role> getAll();

    boolean existsByName(String name);
}