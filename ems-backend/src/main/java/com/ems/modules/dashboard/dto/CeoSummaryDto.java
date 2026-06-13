package com.ems.modules.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CeoSummaryDto {
    private BigDecimal totalKwhToday;
    private BigDecimal totalCostTodayInr;
    private BigDecimal savingsVsYesterdayPct;
    private BigDecimal currentDemandKw;
    private BigDecimal powerFactor;
    private Integer activeAlertCount;
}
