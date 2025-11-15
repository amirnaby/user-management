package com.niam.usermanagement.payload.request;

import com.niam.usermanagement.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @StrongPassword
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}