package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

public interface UserService {
    Long getCurrentUserId(Authentication authentication);

    Page<User> getAllUsers(PageRequest pageRequest);

    User loadUserByUsername(String username);

    User createUser(UserDTO request);

    void updateUser(User user);

    User updateUser(String username, UserDTO request);

    void deleteUser(String username);

    User updateProfile(UserDTO request);
}