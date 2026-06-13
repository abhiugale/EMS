package com.ems.modules.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeDto {
    private Instant timestamp;
    private BigDecimal activeKw;
    private BigDecimal apparentKva;
    private BigDecimal powerFactor;
    private BigDecimal frequency;
    private BigDecimal voltage;
    private BigDecimal current;
}
