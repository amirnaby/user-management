package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final ResponseEntityUtil responseEntityUtil;

    @HasPermission(PRIVILEGE.ROLE_MANAGE)
    @PostMapping
    public ResponseEntity<ServiceResponse> createRole(@RequestBody Role role) {
        return responseEntityUtil.ok(roleService.createRole(role));
    }

    @HasPermission(PRIVILEGE.ROLE_MANAGE)
    @PutMapping
    public ResponseEntity<ServiceResponse> updateRole(@RequestBody Role role) {
        return responseEntityUtil.ok(roleService.updateRole(role));
    }

    @HasPermission(PRIVILEGE.ROLE_MANAGE)
    @DeleteMapping("/{roleName}")
    public ResponseEntity<ServiceResponse> deleteRole(@PathVariable String roleName) {
        roleService.deleteRole(roleName);
        return responseEntityUtil.ok("Role has been deleted");
    }

    @HasPermission(PRIVILEGE.ROLE_MANAGE)
    @GetMapping("/{roleName}")
    public ResponseEntity<ServiceResponse> getRole(@PathVariable String roleName) {
        return responseEntityUtil.ok(roleService.getByName(roleName));
    }

    @HasPermission(PRIVILEGE.ROLE_MANAGE)
    @GetMapping()
    public ResponseEntity<ServiceResponse> getAllRole() {
        return responseEntityUtil.ok(roleService.getAll());
    }
}