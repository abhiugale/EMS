package com.ems.modules.energy.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDto {
    private BigDecimal totalKwh;
    private BigDecimal totalCost;
    private BigDecimal averagePowerFactor;
    private List<HourlyPoint> hourlyCurve;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyPoint {
        private Integer hour;
        private BigDecimal maxKw;
        private BigDecimal avgKw;
    }
}
