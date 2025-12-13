package com.niam.usermanagement.model.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.niam.usermanagement.annotation.StrongPassword;
import com.niam.usermanagement.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    @NotBlank(message = "firstname is required")
    private String firstname;
    @NotBlank(message = "lastname is required")
    private String lastname;
    @NotBlank(groups = {UserService.class}, message = "username is required")
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(groups = {StrongPassword.class}, message = "password is required")
    @StrongPassword(groups = {StrongPassword.class})
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
    @Builder.Default
    private Set<String> roleNames = new HashSet<>();
}