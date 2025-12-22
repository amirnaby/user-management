package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.entities.UserGroup;
import com.niam.usermanagement.model.repository.UserGroupRepository;
import com.niam.usermanagement.service.GroupService;
import com.niam.usermanagement.service.PermissionService;
import com.niam.usermanagement.service.RoleService;
import com.niam.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final UserGroupRepository groupRepository;
    private final RoleService roleService;
    private final UserService userService;
    private final PermissionService permissionService;

    @Override
    public UserGroup createGroup(String name, String desc) {
        return groupRepository.save(UserGroup.builder().name(name).description(desc).build());
    }

    @Override
    public UserGroup assignRole(Long groupId, String roleName) {
        UserGroup g = groupRepository.findById(groupId).orElseThrow();
        Role r = roleService.getByName(roleName);
        g.getRoles().add(r);
        UserGroup saved = groupRepository.save(g);
        permissionService.invalidateAll();
        return saved;
    }

    @Override
    public UserGroup addMember(Long groupId, String username) {
        UserGroup g = groupRepository.findById(groupId).orElseThrow();
        User u = userService.getUserByUsername(username);
        u.getGroups().add(g);
        userService.updateUser(u);
        UserGroup saved = groupRepository.save(g);
        permissionService.invalidateUser(u.getId());
        return saved;
    }
}