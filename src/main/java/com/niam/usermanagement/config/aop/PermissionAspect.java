package com.niam.usermanagement.config.aop;

import com.niam.usermanagement.annotation.HasPermission;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(hasPermission)")
    public void checkPermission(HasPermission hasPermission) {
        String required = hasPermission.value();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean allowed = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(required));

        if (!allowed) {
            throw new AccessDeniedException("Permission denied: " + required);
        }
    }
}