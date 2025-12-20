package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Set;

public interface UserService {
    User getCurrentUser();

    UserDTO getCurrentUserDTO();

    User getUserByUsername(String username);

    Page<User> getAllUsers(PageRequest pageRequest);

    User loadUserByUsername(String username);

    User createUser(UserDTO userDTO);

    void updateUser(User user);

    User updateUser(String username, UserDTO request);

    void deleteUser(String username);

    User updateProfile(UserDTO request);

    User updateRoles(String username, Set<String> roleNames);
}