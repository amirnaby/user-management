package com.niam.usermanagement.service.impl;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.common.exception.IllegalArgumentException;
import com.niam.common.exception.ValidationException;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.exception.AuthenticationException;
import com.niam.usermanagement.model.entities.PasswordHistory;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.AuthenticationRequest;
import com.niam.usermanagement.model.payload.request.ChangePasswordRequest;
import com.niam.usermanagement.model.payload.request.ResetPasswordRequest;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.model.payload.response.AuthenticationResponse;
import com.niam.usermanagement.model.repository.PasswordHistoryRepository;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.*;
import com.niam.usermanagement.utils.AuthUtils;
import com.niam.usermanagement.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional("transactionManager")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AttemptService attemptService;
    private final AccountLockService accountLockService;
    private final DeviceSessionService deviceSessionService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordExpirationService passwordExpirationService;
    private final AuditLogService auditLogService;
    private final UMConfigFile configFile;
    private final AuthUtils authUtils;


    @Override
    public AuthenticationResponse register(UserDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("Mobile already exists");
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("Default role not found"));
        User user = new User();
        BeanUtils.copyProperties(request, user, "roleName");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(defaultRole);
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(Instant.now());
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
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest servletRequest) {
        String ip = RequestUtils.getClientIp(servletRequest);
        String userAgent = RequestUtils.getUserAgent(servletRequest);
        String username = request.getUsername();

        // quick checks before auth: are we already blocked?
        if (attemptService.isIpBlocked(ip)) {
            throw new AuthenticationException("Too many requests from your IP");
        }
        if (attemptService.isUsernameBlocked(username)) {
            throw new AuthenticationException("Too many login attempts for this username");
        }

        try {
            if (configFile.isPasswordlessEnabled()) userService.loadUserByUsername(username);
            else
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        } catch (BadCredentialsException | EntityNotFoundException ex) {
            boolean ipStillAllowed = attemptService.registerFailureForIp(ip);
            boolean userStillAllowed = attemptService.registerFailureForUsername(username);

            auditLogService.log(null, username, "LOGIN_FAIL", ip, userAgent);

            if (!userStillAllowed) {
                accountLockService.lock(userService.loadUserByUsername(username));
            }

            if (!ipStillAllowed) {
                throw new AuthenticationException("Too many login attempts from your IP");
            }

            throw ex;
        }

        attemptService.registerSuccess(username, ip);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        if (!user.isEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        accountLockService.unlockIfExpired(user);
        if (user.isAccountLocked()) {
            throw new AuthenticationException("Account locked until: " + user.getLockUntil());
        }

        if (passwordExpirationService.isExpired(user)) {
            user.setMustChangePassword(true);
            userRepository.save(user);
            return AuthenticationResponse.builder().mustChangePassword(true).build();
        }

        deviceSessionService.registerLogin(user.getId(), ip, userAgent);

        String jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        auditLogService.log(user.getId(), user.getUsername(), "LOGIN_SUCCESS", ip, userAgent);

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .roles(roles)
                .username(user.getUsername())
                .id(user.getId())
                .refreshToken(refreshToken.getToken())
                .tokenType("BEARER")
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request, HttpServletRequest servletRequest) {
        String ip = RequestUtils.getClientIp(servletRequest);
        String userAgent = RequestUtils.getUserAgent(servletRequest);

        User currentUser = authUtils.getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new ValidationException("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new ValidationException("New password must be different from old password");
        }

        checkPasswordHistoryAndSave(currentUser, request.getNewPassword(), false);

        passwordHistoryRepository.save(
                PasswordHistory.builder()
                        .userId(currentUser.getId())
                        .oldPasswordHash(currentUser.getPassword())
                        .changedAt(Instant.now())
                        .build()
        );

        auditLogService.log(null, currentUser.getUsername(), "PASS_CHANGED", ip, userAgent);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, HttpServletRequest servletRequest) {
        String ip = RequestUtils.getClientIp(servletRequest);
        String userAgent = RequestUtils.getUserAgent(servletRequest);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        checkPasswordHistoryAndSave(user, request.getNewPassword(), true);

        passwordHistoryRepository.save(
                PasswordHistory.builder()
                        .userId(user.getId())
                        .oldPasswordHash(user.getPassword())
                        .changedAt(Instant.now())
                        .build()
        );

        auditLogService.log(null, user.getUsername(), "PASS_RESET", ip, userAgent);
    }

    private void checkPasswordHistoryAndSave(User user, String newPassword, boolean mcp) {
        List<PasswordHistory> last = passwordHistoryRepository.findTop5ByUserIdOrderByChangedAtDesc(user.getId());

        for (PasswordHistory ph : last) {
            if (passwordEncoder.matches(newPassword, ph.getOldPasswordHash()))
                throw new ValidationException("Your new password has been used recently.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        if (mcp) user.setMustChangePassword(true);
        passwordExpirationService.markChanged(user);
        userRepository.save(user);

        refreshTokenService.revokeTokensByUser(user);
    }
}