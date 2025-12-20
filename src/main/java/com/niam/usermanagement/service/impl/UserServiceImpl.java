package com.niam.usermanagement.service.impl;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.usermanagement.exception.UserNotFoundException;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.RefreshTokenService;
import com.niam.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Override
    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Invalid username or password"));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Invalid username"));
    }

    @Override
    public Page<User> getAllUsers(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest);
    }

    @Override
    public User getCurrentUser() {
        String token = jwtService.getJwtFromRequest();
        String username = jwtService.extractUsername(token);
        return loadUserByUsername(username);
    }

    @Override
    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        UserDTO userDto = new UserDTO();
        BeanUtils.copyProperties(user, userDto);
        userDto.setRoleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return userDto;
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    @Override
    public User createUser(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user, "roleName");
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new EntityNotFoundException("Role not found"));
        user.getRoles().add(role);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return userRepository.save(user);
    }


    @Transactional("transactionManager")
    @Override
    public void updateUser(User updated) {
        User existing = userRepository.findByUsername(updated.getUsername()).orElseThrow(() -> new UserNotFoundException("User not found"));
        BeanUtils.copyProperties(updated, existing);
        if (updated.getRoles() != null && !updated.getRoles().isEmpty()) {
            Set<Role> newRoles = updated.getRoles().stream().map(n -> roleRepository.findByName(n.getName()).orElseThrow(() -> new EntityNotFoundException("Role not found: " + n))).collect(Collectors.toSet());
            existing.setRoles(newRoles);
        }
        userRepository.save(existing);
    }

    @Transactional("transactionManager")
    @Override
    public User updateUser(String username, UserDTO request) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        BeanUtils.copyProperties(request, user, "roleNames");
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            Set<Role> newRoles = request.getRoleNames().stream().map(n -> roleRepository.findByName(n).orElseThrow(() -> new EntityNotFoundException("Role not found: " + n))).collect(Collectors.toSet());
            user.setRoles(newRoles);
        }
        return userRepository.save(user);
    }

    @Transactional("transactionManager")
    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        refreshTokenService.revokeTokensByUser(user);
        userRepository.delete(user);
    }

    @Transactional("transactionManager")
    @Override
    public User updateProfile(UserDTO request) {
        User currentUser = getCurrentUser();
        BeanUtils.copyProperties(request, currentUser, "username", "password", "roleNames");
        return userRepository.save(currentUser);
    }

    @Transactional("transactionManager")
    @Override
    public void updateRoles(String username, Set<String> newRoleNames) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));

        if (newRoleNames == null || newRoleNames.isEmpty()) {
            user.getRoles().clear();
        } else {
            Set<Role> newRoles = newRoleNames.stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new EntityNotFoundException("Role " + roleName + " not found")))
                    .collect(Collectors.toSet());

            user.getRoles().clear();
            user.getRoles().addAll(newRoles);
        }

        userRepository.save(user);
    }
}