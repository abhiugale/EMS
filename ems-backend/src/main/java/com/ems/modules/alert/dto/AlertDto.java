package com.ems.modules.alert.dto;

import com.ems.common.enums.AlertSeverity;
import com.ems.common.enums.AlertType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
    private UUID id;
    private UUID factoryId;
    private UUID machineId;
    private String machineName;
    private AlertType alertType;
    private AlertSeverity severity;
    private String message;
    private String status;
    private BigDecimal thresholdValue;
    private BigDecimal actualValue;
    private Instant createdAt;
    private Instant resolvedAt;
    private String resolvedByEmail;
}
