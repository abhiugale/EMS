package com.ems.modules.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeakDemandDto {
    private Instant peakTime;
    private BigDecimal peakDemandKw;
    private BigDecimal contractDemandKw;
    private boolean limitBreached;
}
