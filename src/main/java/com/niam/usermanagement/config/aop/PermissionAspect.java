package com.niam.usermanagement.config.aop;

import com.niam.usermanagement.annotation.HasPermission;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(hasPermission)")
    public void checkPermission(HasPermission hasPermission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var userPerms = auth.getAuthorities();

        String[] required = Arrays.stream(hasPermission.value()).map(Object::toString).toArray(String[]::new);

        boolean allowed;
        if (userPerms.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) allowed = true;
        else allowed = userPerms.stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(Arrays.asList(required)::contains);

        if (!allowed) {
            throw new AccessDeniedException("Permission denied. Required: " + Arrays.toString(required));
        }
    }
}