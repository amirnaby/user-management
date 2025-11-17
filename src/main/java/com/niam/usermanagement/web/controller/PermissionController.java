package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.payload.response.PermissionDto;
import com.niam.usermanagement.service.PermissionCacheService;
import com.niam.usermanagement.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Returns permissions for the current authenticated user.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionCacheService permissionCacheService;
    private final UserQueryService userQueryService;

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionDto>> getPermissions(Authentication authentication) {
        Long userId = userQueryService.getCurrentUserId(authentication);
        List<Permission> perms = permissionCacheService.getPermissionsForUser(userId);
        List<PermissionDto> dto = perms.stream()
                .map(p -> new PermissionDto(p.getCode(), p.getDescription()))
                .toList();
        return ResponseEntity.ok(dto);
    }
}