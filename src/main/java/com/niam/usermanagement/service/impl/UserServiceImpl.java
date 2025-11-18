package com.niam.usermanagement.service.impl;

import com.niam.common.utils.GenericDtoMapper;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.RefreshTokenService;
import com.niam.usermanagement.service.UserService;
import com.niam.usermanagement.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthUtils authUtils;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Long getCurrentUserId(Authentication authentication) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String token = jwtService.getJwtFromRequest(request);
        return jwtService.extractUserId(token);
    }

    @Transactional("transactionManager")
    @Override
    public void createUser(UserDTO request) {
        User user = new User();
        GenericDtoMapper.copyNonNullProperties(request, user, "roleName");
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.getRoles().add(role);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Transactional("transactionManager")
    @Override
    public void updateUser(String username, UserDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        GenericDtoMapper.copyNonNullProperties(request, user, "roleNames");
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            Set<Role> newRoles = request.getRoleNames().stream()
                    .map(n -> roleRepository.findByName(n)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + n)))
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }
        userRepository.save(user);
    }

    @Transactional("transactionManager")
    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        refreshTokenService.revokeTokensByUser(user);
        userRepository.delete(user);
    }

    @Transactional("transactionManager")
    @Override
    public void updateProfile(UserDTO request) {
        User currentUser = authUtils.getCurrentUser();
        GenericDtoMapper.copyNonNullProperties(request, currentUser);
        userRepository.save(currentUser);
    }
}