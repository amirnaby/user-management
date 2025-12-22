package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.service.PermissionService;
import com.niam.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Permission Management", description = "Permission endpoints")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    private final UserService userService;
    private final ResponseEntityUtil responseEntityUtil;

    /**
     * Returns permissions for the current authenticated user.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ServiceResponse> getPermissionsForUser() {
        Long userId = userService.getCurrentUser().getId();
        List<Permission> perms = permissionService.getPermissionsForUser(userId);
        return responseEntityUtil.ok(perms);
    }

    @HasPermission(PRIVILEGE.PERMISSION_MANAGE)
    @PostMapping
    public ResponseEntity<ServiceResponse> createPermission(@RequestBody Permission permission) {
        return responseEntityUtil.ok(permissionService.create(permission));
    }

    @HasPermission(PRIVILEGE.PERMISSION_MANAGE)
    @DeleteMapping("/{permissionCode}")
    public ResponseEntity<ServiceResponse> deletePermission(@PathVariable String permissionCode) {
        permissionService.deletePermission(permissionCode);
        return responseEntityUtil.ok("Permission has been deleted!");
    }

    @HasPermission(PRIVILEGE.PERMISSION_MANAGE)
    @GetMapping("/all")
    public ResponseEntity<ServiceResponse> getAllPermissions() {
        return responseEntityUtil.ok(permissionService.getAll());
    }
}