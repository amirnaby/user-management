package com.niam.usermanagement.model.payload.request;

import com.niam.usermanagement.annotation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotNull
    private Long userId;
    @NotBlank
    @StrongPassword
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}