package com.ems.modules.energy.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownDto {

    private List<BreakdownItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownItem {
        private String name;
        private BigDecimal kwh;
        private BigDecimal cost;
        private BigDecimal percentage;
    }
}
