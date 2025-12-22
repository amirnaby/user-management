package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.annotation.StrongPassword;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.service.UserService;
import com.niam.common.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Tag(name = "User Management Admin", description = "Admin endpoints")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final PaginationUtils paginationUtils;
    private final ResponseEntityUtil responseEntityUtil;

    @PostMapping("/users")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<ServiceResponse> createUserByAdmin(@Validated({Default.class, StrongPassword.class}) @RequestBody UserDTO request) {
        return responseEntityUtil.ok(userService.createUser(request));
    }

    @PutMapping("/users/{username}")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<ServiceResponse> updateUser(@PathVariable String username, @RequestBody UserDTO request) {
        return responseEntityUtil.ok(userService.updateUserDTO(username, request));
    }

    @DeleteMapping("/users/{username}")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<ServiceResponse> deleteUser(@PathVariable String username) {
        userService.deactivateUser(username);
        return responseEntityUtil.ok("User deleted successfully");
    }

    @PutMapping("/users/{username}/roles")
    @HasPermission(PRIVILEGE.USER_MANAGE)
    public ResponseEntity<ServiceResponse> updateRoles(@PathVariable String username, @RequestBody Set<String> roleNames) {
        return responseEntityUtil.ok(userService.updateRoles(username, roleNames));
    }

    @GetMapping("/users/{username}")
    @HasPermission({PRIVILEGE.USER_MANAGE, PRIVILEGE.USER_READ})
    public ResponseEntity<ServiceResponse> getUser(@PathVariable String username) {
        return responseEntityUtil.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/users")
    @HasPermission({PRIVILEGE.USER_MANAGE, PRIVILEGE.USER_READ})
    public ResponseEntity<ServiceResponse> getAllUsers(@RequestParam Map<String, Object> requestParams) {
        return responseEntityUtil.ok(userService.getAllUsers(paginationUtils.pageHandler(requestParams)));
    }
}