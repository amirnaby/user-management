package com.niam.usermanagement.config.init;

import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.model.enums.ROLE;
import com.niam.usermanagement.model.repository.PermissionRepository;
import com.niam.usermanagement.model.repository.RoleRepository;
import com.niam.usermanagement.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("init")
@Component
@RequiredArgsConstructor
public class StartupDataLoader implements ApplicationRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // ---------------------------------------------------------
        // 1) Seed permissions from PRIVILEGE enum
        // ---------------------------------------------------------
        for (PRIVILEGE privilege : PRIVILEGE.values()) {
            permissionRepository.findByCode(privilege.getCode())
                    .orElseGet(() -> permissionRepository.save(
                            Permission.builder()
                                    .code(privilege.getCode())
                                    .name(privilege.getCode())
                                    .description(privilege.getCode())
                                    .build()
                    ));
        }

        // ---------------------------------------------------------
        // 2) Seed roles from ROLE enum
        // ---------------------------------------------------------
        for (ROLE roleEnum : ROLE.values()) {
            String roleName = "ROLE_" + roleEnum.name(); // ROLE_ADMIN, ROLE_USER ...

            if (!roleRepository.existsByName(roleName)) {

                Set<Permission> permissions = roleEnum.getPRIVILEGES()
                        .stream()
                        .map(pr -> permissionRepository.findByCode(pr.getCode())
                                .orElseThrow(() -> new IllegalStateException("Permission not found: " + pr)))
                        .collect(Collectors.toSet());

                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName)
                        .permissions(permissions)
                        .build();

                roleRepository.save(role);
            }
        }

        // ---------------------------------------------------------
        // 3) Create initial default user if not exists
        // ---------------------------------------------------------
        if (!userRepository.existsByUsername("AmirNaby")) {

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

            User user = User.builder()
                    .firstname("Amir")
                    .lastname("Naby")
                    .username("AmirNaby")
                    .password(passwordEncoder.encode("Amir@Naby123"))
                    .email("naby1371@gmail.com")
                    .mobile("09369052885")
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .mustChangePassword(false)
                    .passwordChangedAt(Instant.now())
                    .build();

            userRepository.save(user);
        }
    }
}