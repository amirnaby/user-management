package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Profile", description = "User endpoints")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ResponseEntityUtil responseEntityUtil;

    @PutMapping
    public ResponseEntity<ServiceResponse> updateProfile(@Valid @RequestBody UserDTO request) {
        return responseEntityUtil.ok(userService.updateProfile(request));
    }
}