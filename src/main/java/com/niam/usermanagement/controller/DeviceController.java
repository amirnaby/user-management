package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.service.DeviceSessionService;
import com.niam.usermanagement.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ResponseEntityUtil responseEntityUtil;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ServiceResponse> list(HttpServletRequest request) {
        String token = jwtService.getJwtFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        return responseEntityUtil.ok(deviceService.listSessions(userId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/revoke/{id}")
    public ResponseEntity<ServiceResponse> revoke(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtService.getJwtFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        deviceService.revokeSessionForUser(id, userId);
        return responseEntityUtil.ok(Map.of("message", "revoked"));
    }
}