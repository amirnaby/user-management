package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
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
    private final ResponseEntityUtil responseEntityUtil;

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@RequestBody UserGroup dto) {
        UserGroup created = groupService.createGroup(dto.getName(), dto.getDescription());
        return responseEntityUtil.ok(created);
    }

    @PostMapping("/{groupId}/roles/{roleId}")
    public ResponseEntity<ServiceResponse> assignRole(@PathVariable Long groupId, @PathVariable Long roleId) {
        return responseEntityUtil.ok(groupService.assignRole(groupId, roleId));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ServiceResponse> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        return responseEntityUtil.ok(groupService.addMember(groupId, userId));
    }
}