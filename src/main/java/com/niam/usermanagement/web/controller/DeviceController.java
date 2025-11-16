package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.service.DeviceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceSessionService deviceService;

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(deviceService.listSessions(userId));
    }

    @PostMapping("/revoke/{id}")
    public ResponseEntity<?> revoke(@PathVariable Long id) {
        deviceService.revokeSession(id);
        return ResponseEntity.ok("revoked");
    }
}