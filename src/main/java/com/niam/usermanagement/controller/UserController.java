package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "User endpoints")
@RestController
@RequestMapping("/current-user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ResponseEntityUtil responseEntityUtil;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ServiceResponse> userInfo() {
        return responseEntityUtil.ok(userService.getCurrentUserDTO());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public ResponseEntity<ServiceResponse> updateProfile(@Valid @RequestBody UserDTO request) {
        return responseEntityUtil.ok(userService.updateProfile(request));
    }
}