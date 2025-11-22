package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {
    Long getCurrentUserId(Authentication authentication);

    List<User> getAllUsers();

    User loadUserByUsername(String username);

    User createUser(UserDTO request);

    void updateUser(User user);

    User updateUser(String username, UserDTO request);

    void deleteUser(String username);

    User updateProfile(UserDTO request);
}