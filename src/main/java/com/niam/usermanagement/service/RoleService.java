package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Role;

public interface RoleService {
    Role createRole(String name, String desc);

    Role assignPermission(String roleName, String permissionCode);
}