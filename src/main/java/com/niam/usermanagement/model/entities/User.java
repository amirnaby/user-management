package com.niam.usermanagement.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Entity
@Table(name = "um_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"mobile"})
})
@SequenceGenerator(name = "um_users_seq", sequenceName = "um_users_seq", allocationSize = 1)
public class User extends Auditable implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "um_users_seq")
    private Long id;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String mobile;

    @Column(nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "um_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "um_user_group_membership",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<UserGroup> groups = new HashSet<>();

    @Column(nullable = false)
    private boolean accountLocked = false;

    @Column
    private Instant lockUntil;

    @Column
    private Instant passwordChangedAt;

    @Column
    private boolean mustChangePassword;

    @Column
    private Integer otpSendCount = 0;

    @Column
    private Instant lastOtpSentAt;

    // build authorities from roles + permissions + group roles
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorityStrings = new HashSet<>();

        if (roles != null) {
            for (Role r : roles) {
                if (r.getName() != null) authorityStrings.add(r.getName());
                if (r.getPermissions() != null) {
                    r.getPermissions().forEach(p -> {
                        if (p != null && p.getCode() != null) authorityStrings.add(p.getCode());
                    });
                }
            }
        }

        if (groups != null) {
            for (UserGroup g : groups) {
                if (g.getRoles() != null) {
                    for (Role r : g.getRoles()) {
                        if (r.getName() != null) authorityStrings.add(r.getName());
                        if (r.getPermissions() != null) {
                            r.getPermissions().forEach(p -> {
                                if (p != null && p.getCode() != null) authorityStrings.add(p.getCode());
                            });
                        }
                    }
                }
            }
        }

        return authorityStrings.stream()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}