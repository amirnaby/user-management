package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.model.entities.UserGroup;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Group and Role assignment management. Only ADMIN can access.
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final ResponseEntityUtil responseEntityUtil;

    @HasPermission(PRIVILEGE.GROUP_MANAGE)
    @PostMapping
    public ResponseEntity<ServiceResponse> create(@RequestBody UserGroup dto) {
        return responseEntityUtil.ok(groupService.createGroup(dto.getName(), dto.getDescription()));
    }

    @HasPermission(PRIVILEGE.GROUP_MANAGE)
    @PostMapping("/{groupId}/roles/{roleName}")
    public ResponseEntity<ServiceResponse> assignRole(@PathVariable Long groupId, @PathVariable String roleName) {
        return responseEntityUtil.ok(groupService.assignRole(groupId, roleName));
    }

    @HasPermission(PRIVILEGE.GROUP_MANAGE)
    @PostMapping("/{groupId}/members/{username}")
    public ResponseEntity<ServiceResponse> addMember(@PathVariable Long groupId, @PathVariable String username) {
        return responseEntityUtil.ok(groupService.addMember(groupId, username));
    }
}