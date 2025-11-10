package com.niam.usermanagement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.niam.usermanagement.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// make our app User a spring security User
// we have two options : implements the UserDetails interface or create a user class that extends User spring class which also implements UserDetails
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Entity(name = "Users")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"username"}))
@SequenceGenerator(name = "Users_seq", sequenceName = "Users_seq", allocationSize = 1)
public class User extends Auditable implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Users_seq")
    private Long id;
    private String firstname;
    private String lastname;
    @Column(unique = true)
    private String username;
    private String password;
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    private Role role;

    // we should return a list of roles
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
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
        return true;
    }
}