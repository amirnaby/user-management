package com.niam.usermanagement.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "um_menu_permissions", joinColumns = @JoinColumn(name = "menu_id"))
    @Column(name = "permission")
    private Set<String> permissions;
}