package com.ems.modules.machine.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineCardDto {
    private UUID id;
    private String name;
    private String department;
    private String machineType;
    private String status; // RUNNING/IDLE/OFF/ALERT
    private BigDecimal todayKwh;
    private BigDecimal kwhPerPart;
    private Integer partsToday;
    private BigDecimal efficiencyPct;
    private boolean anomalyFlag;
    private Instant lastSeen;
}
