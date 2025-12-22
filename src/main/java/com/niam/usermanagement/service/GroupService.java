package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.UserGroup;

public interface GroupService {
    UserGroup createGroup(String name, String desc);

    UserGroup assignRole(Long groupId, String roleName);

    UserGroup addMember(Long groupId, String username);
}