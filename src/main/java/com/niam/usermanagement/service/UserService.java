package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    Long getCurrentUserId(Authentication authentication);

    User getUserById(Long userId);

    List<User> getAllUsers();

    UserDetails loadUserByUsername(String username);

    @Transactional("transactionManager")
    void createUser(UserDTO request);

    @Transactional("transactionManager")
    void updateUser(String username, UserDTO request);

    @Transactional("transactionManager")
    void deleteUser(String username);

    @Transactional("transactionManager")
    void updateProfile(UserDTO request);
}