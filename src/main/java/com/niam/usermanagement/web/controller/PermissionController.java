package com.niam.usermanagement.web.controller;

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

    /**
     * Returns permissions for the current authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<Permission>> getPermissions(Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);
        List<Permission> perms = permissionService.getPermissionsForUser(userId);
        return ResponseEntity.ok(perms);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Permission> create(@RequestBody Permission dto) {
        Permission p = permissionService.create(dto);
        return ResponseEntity.ok(p);
    }
}