package com.ems.modules.energy.entity;

import com.ems.modules.machine.entity.Machine;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "energy_readings")
@IdClass(EnergyReadingId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Id
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "energy_kwh", precision = 10, scale = 3)
    private BigDecimal energyKwh;

    @Column(name = "active_kw", precision = 8, scale = 2)
    private BigDecimal activeKw;

    @Column(name = "apparent_kva", precision = 8, scale = 2)
    private BigDecimal apparentKva;

    @Column(name = "reactive_kvar", precision = 8, scale = 2)
    private BigDecimal reactiveKvar;

    @Column(name = "power_factor", precision = 4, scale = 3)
    private BigDecimal powerFactor;

    @Column(precision = 5, scale = 2)
    private BigDecimal frequency;

    @Column(name = "voltage_r", precision = 7, scale = 2)
    private BigDecimal voltageR;

    @Column(name = "voltage_y", precision = 7, scale = 2)
    private BigDecimal voltageY;

    @Column(name = "voltage_b", precision = 7, scale = 2)
    private BigDecimal voltageB;

    @Column(name = "current_r", precision = 7, scale = 2)
    private BigDecimal currentR;

    @Column(name = "current_y", precision = 7, scale = 2)
    private BigDecimal currentY;

    @Column(name = "current_b", precision = 7, scale = 2)
    private BigDecimal currentB;

    @Column(name = "parts_produced")
    private Integer partsProduced;

    @Column(name = "upload_id")
    private UUID uploadId;

    @Column(length = 20)
    private String source;
}
