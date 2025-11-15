package com.niam.usermanagement.init;

import com.niam.usermanagement.entities.Permission;
import com.niam.usermanagement.entities.Role;
import com.niam.usermanagement.enums.Privilege;
import com.niam.usermanagement.repository.PermissionRepository;
import com.niam.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StartupDataLoader implements ApplicationRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (Privilege sp : Privilege.values()) {
            permissionRepository.findByCode(sp.getCode())
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .code(sp.getCode())
                            .name(sp.getCode())
                            .description(sp.getCode())
                            .build()));
        }

        Map<String, Set<String>> roleToPerms = new HashMap<>();
        roleToPerms.put("ROLE_ADMIN", Arrays.stream(Privilege.values()).map(Privilege::getCode).collect(Collectors.toSet()));
        roleToPerms.put("ROLE_USER", Set.of(Privilege.READ_PRIVILEGE.getCode(), Privilege.WRITE_PRIVILEGE.getCode()));
        roleToPerms.put("ROLE_OPERATOR", Set.of(Privilege.READ_PRIVILEGE.getCode(), Privilege.WRITE_PRIVILEGE.getCode()));

        for (Map.Entry<String, Set<String>> e : roleToPerms.entrySet()) {
            String roleName = e.getKey();
            if (!roleRepository.existsByName(roleName)) {
                Set<Permission> perms = e.getValue().stream()
                        .map(code -> permissionRepository.findByCode(code).orElseThrow())
                        .collect(Collectors.toSet());
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName)
                        .permissions(perms)
                        .build();
                roleRepository.save(role);
            }
        }
    }
}