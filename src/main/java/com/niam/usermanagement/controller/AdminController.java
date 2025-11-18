package com.niam.usermanagement.controller;

import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management Admin", description = "Admin endpoints")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @PostMapping("/users")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<?> createUserByAdmin(@Valid @RequestBody UserDTO request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/users/{username}")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody UserDTO request) {
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    @DeleteMapping("/users/{username}")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/users/{username}")
    @HasPermission({PRIVILEGE.USER_MANAGE, PRIVILEGE.USER_READ})
    public ResponseEntity<?> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.loadUserByUsername(username));
    }

    @GetMapping("/users")
    @HasPermission({PRIVILEGE.USER_MANAGE, PRIVILEGE.USER_READ})
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}