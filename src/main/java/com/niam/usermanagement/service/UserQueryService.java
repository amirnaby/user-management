package com.niam.usermanagement.service;

import org.springframework.security.core.Authentication;

public interface UserQueryService {
    Long getCurrentUserId(Authentication authentication);
}
