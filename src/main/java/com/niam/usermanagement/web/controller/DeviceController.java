package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.service.DeviceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Device sessions for logged-in user.
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceSessionService deviceService;

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(deviceService.listSessions(userId));
    }

    @PostMapping("/revoke/{id}")
    public ResponseEntity<?> revoke(@PathVariable Long id, Authentication auth) {
        Long userId = extractUserId(auth);
        deviceService.revokeSessionForUser(id, userId);
        return ResponseEntity.ok(Map.of("message", "revoked"));
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null)
            throw new IllegalStateException("Authentication not found");

        Object principal = auth.getPrincipal();

        if (principal instanceof com.niam.usermanagement.model.entities.User u) {
            return u.getId();
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            throw new IllegalStateException("UserDetails does not contain userId. Ensure custom UserDetails is used.");
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }
}