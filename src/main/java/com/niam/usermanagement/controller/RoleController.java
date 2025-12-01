package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
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
    private final ResponseEntityUtil responseEntityUtil;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ServiceResponse> create(@RequestBody Role dto) {
        Role r = roleService.createRole(dto.getName(), dto.getDescription());
        return responseEntityUtil.ok(r);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{roleName}/permissions")
    public ResponseEntity<ServiceResponse> assignPermissions(
            @PathVariable String roleName,
            @RequestBody List<String> permissionCodes) {
        return responseEntityUtil.ok(roleService.assignPermissions(roleName, permissionCodes));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/roles/{roleName}")
    public ResponseEntity<ServiceResponse> deleteRole(@PathVariable String roleName) {
        roleService.deleteRole(roleName);
        return responseEntityUtil.ok("Role has been deleted");
    }
}