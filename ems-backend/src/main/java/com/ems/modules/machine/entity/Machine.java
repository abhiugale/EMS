package com.ems.modules.machine.entity;

import com.ems.modules.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "machines", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"factory_id", "name"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String department;

    @Column(name = "machine_type", length = 100)
    private String machineType;

    @Column(name = "baseline_kwh", nullable = false, precision = 10, scale = 3)
    private BigDecimal baselineKwh;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private java.time.Instant createdAt;

    @Column(name = "updated_at")
    private java.time.Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.Instant.now();
        updatedAt = java.time.Instant.now();
        if (baselineKwh == null) {
            baselineKwh = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.Instant.now();
    }
}
