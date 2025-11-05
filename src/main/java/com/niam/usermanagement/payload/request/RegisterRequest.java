package com.niam.usermanagement.payload.request;

import com.niam.usermanagement.enums.Role;
import com.niam.usermanagement.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "firstname is required")
    private String firstname;
    @NotBlank(message = "lastname is required")
    private String lastname;
    @NotBlank(message = "code is required")
    private String code;
    @NotBlank(message = "password is required")
    @StrongPassword
    private String password;
    @NotNull
    private Role role;
}