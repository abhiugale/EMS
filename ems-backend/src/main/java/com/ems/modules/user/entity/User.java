package com.ems.modules.user.entity;

import com.ems.common.enums.Role;
import com.ems.modules.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private java.time.Instant createdAt;

    @Column(name = "updated_at")
    private java.time.Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.Instant.now();
        updatedAt = java.time.Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.Instant.now();
    }
}
