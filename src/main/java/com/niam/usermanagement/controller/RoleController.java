package com.niam.usermanagement.controller;

import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{roleName}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> assignPermissions(
            @PathVariable String roleName,
            @RequestBody List<String> permissionCodes) {
        return ResponseEntity.ok(roleService.assignPermissions(roleName, permissionCodes));
    }

    @DeleteMapping("/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleName) {
        roleService.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }
}