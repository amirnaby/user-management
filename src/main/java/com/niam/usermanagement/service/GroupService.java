package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.UserGroup;

public interface GroupService {
    UserGroup createGroup(String name, String desc);

    UserGroup assignRole(Long groupId, Long roleId);

    UserGroup addMember(Long groupId, Long userId);
}