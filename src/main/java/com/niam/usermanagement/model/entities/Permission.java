package com.niam.usermanagement.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "um_permissions", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Permission extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code; // e.g. USER_CREATE
    private String name;
    private String description;
}