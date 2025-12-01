package com.niam.usermanagement.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "um_menu")
public class Menu extends Auditable {
    @Id
    private Long id;
    @Column(nullable = false)
    private String label;
    private String icon;
    @Column(nullable = false, unique = true)
    private String route;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "um_menu_permissions", joinColumns = @JoinColumn(name = "menu_id"))
    @Column(name = "permissions")
    private Set<String> permissions = new HashSet<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "um_menu_roles", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "roles")
    private Set<String> roles = new HashSet<>();
}