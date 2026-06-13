package com.ems.modules.insight.entity;

import com.ems.common.enums.InsightType;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.machine.entity.Machine;
import com.ems.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_insights")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private Machine machine;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false)
    private InsightType insightType;

    @Column(nullable = false)
    private String message;

    @Column(name = "savings_potential_kwh", precision = 10, scale = 2)
    private BigDecimal savingsPotentialKwh;

    @Column(name = "savings_potential_inr", precision = 10, scale = 2)
    private BigDecimal savingsPotentialInr;

    @Column(nullable = false, length = 20)
    private String status; // OPEN, RESOLVED

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = "OPEN";
        }
    }
}
