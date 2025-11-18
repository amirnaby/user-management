package com.niam.usermanagement.web.controller;

import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management Admin", description = "Admin endpoints")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @PostMapping("/users")
    @HasPermission("USER_CREATE")
    public ResponseEntity<?> createUserByAdmin(@RequestBody UserDTO request) {
        userService.createUser(request);
        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping("/users/{username}")
    @HasPermission("USER_EDIT")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody UserDTO request) {
        userService.updateUser(username, request);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/users/{username}")
    @HasPermission("USER_DELETE")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE') and hasRole('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.loadUserByUsername(username));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE') and hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}