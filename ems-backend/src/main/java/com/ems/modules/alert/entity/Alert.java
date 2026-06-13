package com.ems.modules.alert.entity;

import com.ems.common.enums.AlertSeverity;
import com.ems.common.enums.AlertType;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.machine.entity.Machine;
import com.ems.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

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
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, length = 20)
    private String status; // OPEN, RESOLVED

    @Column(name = "threshold_value")
    private BigDecimal thresholdValue;

    @Column(name = "actual_value")
    private BigDecimal actualValue;

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
