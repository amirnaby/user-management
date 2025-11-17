package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserQueryService {
    Long getCurrentUserId(Authentication authentication);

    User getUserById(Long userId);

    UserDetails loadUserByUsername(String username);
}