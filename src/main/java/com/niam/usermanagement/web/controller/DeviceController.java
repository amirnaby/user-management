package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.service.DeviceSessionService;
import com.niam.usermanagement.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        String token = jwtService.getJwtFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(deviceService.listSessions(userId));
    }

    @PostMapping("/revoke/{id}")
    public ResponseEntity<?> revoke(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtService.getJwtFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        deviceService.revokeSessionForUser(id, userId);
        return ResponseEntity.ok(Map.of("message", "revoked"));
    }
}