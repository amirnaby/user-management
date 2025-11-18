package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Role and permission management.
 */
@Tag(name = "Role Management", description = "Role endpoints")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> create(@RequestBody Role dto) {
        Role r = roleService.createRole(dto.getName(), dto.getDescription());
        return ResponseEntity.ok(r);
    }

    @PostMapping("/{roleName}/permissions/{permissionCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> assignPerm(@PathVariable String roleName, @PathVariable String permissionCode) {
        return ResponseEntity.ok(roleService.assignPermission(roleName, permissionCode));
    }
}