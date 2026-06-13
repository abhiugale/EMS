package com.ems.modules.insight.dto;

import com.ems.common.enums.InsightType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightDto {
    private UUID id;
    private UUID factoryId;
    private UUID machineId;
    private String machineName;
    private InsightType insightType;
    private String message;
    private BigDecimal savingsPotentialKwh;
    private BigDecimal savingsPotentialInr;
    private String status;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant resolvedAt;
    private String resolvedByEmail;
}
