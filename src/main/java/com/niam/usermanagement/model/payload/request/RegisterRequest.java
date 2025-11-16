package com.niam.usermanagement.model.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
    private String password;
    @NotBlank(message = "email is required")
    @Email
    private String email;
    @NotBlank(message = "mobile is required")
    private String mobile;

    /**
     * roleName optional: if null, default role will be used (e.g. ROLE_USER)
     * For safety in production, registration with privilege roles should be prevented,
     * or system should ignore roleName and always assign default.
     */
    private String roleName;
}