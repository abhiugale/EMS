package com.ems.modules.factory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "factories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(length = 50)
    private String timezone;

    @Column(name = "contract_demand_kw", nullable = false)
    private BigDecimal contractDemandKw;

    @Column(name = "tariff_inr_per_kwh", nullable = false)
    private BigDecimal tariffInrPerKwh;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (timezone == null) {
            timezone = "Asia/Kolkata";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
