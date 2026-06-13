package com.ems.modules.machine.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineDto {
    private UUID id;
    private UUID factoryId;
    private String name;
    private String department;
    private String machineType;
    private BigDecimal baselineKwh;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
