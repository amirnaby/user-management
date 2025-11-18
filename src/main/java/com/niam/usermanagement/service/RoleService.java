package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Role;

import java.util.List;

public interface RoleService {
    Role createRole(String name, String desc);

    Role assignPermissions(String roleName, List<String> permissionCodes);

    void deleteRole(String roleName);
}