package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.service.PermissionService;
import com.niam.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @GetMapping
    public ResponseEntity<ServiceResponse> getPermissions(Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);
        List<Permission> perms = permissionService.getPermissionsForUser(userId);
        return responseEntityUtil.ok(perms);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> create(@RequestBody Permission dto) {
        Permission p = permissionService.create(dto);
        return responseEntityUtil.ok(p);
    }

    @DeleteMapping("/permissions/{permissionCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> deletePermission(@PathVariable String permissionCode) {
        permissionService.deletePermission(permissionCode);
        return responseEntityUtil.ok("Permission has been deleted!");
    }

}