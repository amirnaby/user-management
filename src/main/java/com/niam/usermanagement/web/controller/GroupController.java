package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.model.entities.UserGroup;
import com.niam.usermanagement.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Group and Role assignment management. Only ADMIN can access.
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<UserGroup> create(@RequestBody UserGroup dto) {
        UserGroup created = groupService.createGroup(dto.getName(), dto.getDescription());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{groupId}/roles/{roleId}")
    public ResponseEntity<UserGroup> assignRole(@PathVariable Long groupId, @PathVariable Long roleId) {
        return ResponseEntity.ok(groupService.assignRole(groupId, roleId));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<UserGroup> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        return ResponseEntity.ok(groupService.addMember(groupId, userId));
    }
}