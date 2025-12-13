package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface UserService {
    User getCurrentUser();

    UserDTO getCurrentUserDTO();

    Page<User> getAllUsers(PageRequest pageRequest);

    User loadUserByUsername(String username);

    User createUser(UserDTO request);

    void updateUser(User user);

    User updateUser(String username, UserDTO request);

    void deleteUser(String username);

    User updateProfile(UserDTO request);
}