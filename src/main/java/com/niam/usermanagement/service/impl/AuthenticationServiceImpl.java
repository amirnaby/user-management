package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.entities.Role;
import com.niam.usermanagement.entities.User;
import com.niam.usermanagement.payload.request.AuthenticationRequest;
import com.niam.usermanagement.payload.request.ChangePasswordRequest;
import com.niam.usermanagement.payload.request.RegisterRequest;
import com.niam.usermanagement.payload.request.ResetPasswordRequest;
import com.niam.usermanagement.payload.response.AuthenticationResponse;
import com.niam.usermanagement.repository.RoleRepository;
import com.niam.usermanagement.repository.UserRepository;
import com.niam.usermanagement.service.AuthenticationService;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.RefreshTokenService;
import com.niam.usermanagement.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional("transactionManager")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final AuthUtils authUtils;

    @Value("${app.registration.enabled:true}")
    private boolean registrationEnabled;

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        if (!registrationEnabled) {
            throw new IllegalStateException("User registration is disabled");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("Mobile already exists");
        }

        Role role = null;
        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.getRoleName()));
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .mobile(request.getMobile())
                .build();

        if (role != null) {
            user.getRoles().add(role);
        }

        user = userRepository.save(user);

        var jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .username(user.getUsername())
                .id(user.getId())
                .refreshToken(refreshToken.getToken())
                .roles(roles)
                .tokenType("BEARER")
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        var jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .roles(roles)
                .username(user.getUsername())
                .id(user.getId())
                .refreshToken(refreshToken.getToken())
                .tokenType("BEARER")
                .build();
    }

    public void changePassword(ChangePasswordRequest request) {
        User currentUser = authUtils.getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("New password must be different from old password");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        refreshTokenService.revokeTokensByUser(currentUser);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenService.revokeTokensByUser(user);
    }

}