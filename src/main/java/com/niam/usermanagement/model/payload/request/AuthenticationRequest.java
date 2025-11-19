package com.niam.usermanagement.model.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
    private String password;
}