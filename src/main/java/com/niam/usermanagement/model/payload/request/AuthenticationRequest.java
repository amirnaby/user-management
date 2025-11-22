package com.niam.usermanagement.model.payload.request;

import com.niam.usermanagement.annotation.StrongPassword;
import com.niam.usermanagement.service.otp.provider.OtpProvider;
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
    @NotBlank(groups = StrongPassword.class, message = "password is required")
    private String password;
    @NotBlank(groups = OtpProvider.class, message = "otp is required")
    private String otp;
}