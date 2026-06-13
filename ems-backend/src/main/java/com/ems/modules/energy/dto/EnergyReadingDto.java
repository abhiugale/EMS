package com.ems.modules.energy.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyReadingDto {
    private String machineName;
    private Instant recordedAt;
    private BigDecimal energyKwh;
    private BigDecimal activeKw;
    private BigDecimal apparentKva;
    private BigDecimal reactiveKvar;
    private BigDecimal powerFactor;
    private BigDecimal frequency;
    private BigDecimal voltageR;
    private BigDecimal voltageY;
    private BigDecimal voltageB;
    private BigDecimal currentR;
    private BigDecimal currentY;
    private BigDecimal currentB;
    private Integer partsProduced;
    private String source;
}
