package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.entities.UserGroup;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserGroupRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.GroupService;
import com.niam.usermanagement.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final UserGroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionCacheService permissionCacheService;

    @Override
    public UserGroup createGroup(String name, String desc) {
        return groupRepository.save(UserGroup.builder().name(name).description(desc).build());
    }

    @Override
    public UserGroup assignRole(Long groupId, Long roleId) {
        UserGroup g = groupRepository.findById(groupId).orElseThrow();
        Role r = roleRepository.findById(roleId).orElseThrow();
        g.getRoles().add(r);
        UserGroup saved = groupRepository.save(g);
        permissionCacheService.invalidateAll();
        return saved;
    }

    @Override
    public UserGroup addMember(Long groupId, Long userId) {
        UserGroup g = groupRepository.findById(groupId).orElseThrow();
        User u = userRepository.findById(userId).orElseThrow();
        u.getGroups().add(g);
        userRepository.save(u);
        UserGroup saved = groupRepository.save(g);
        permissionCacheService.invalidateUser(userId);
        return saved;
    }
}