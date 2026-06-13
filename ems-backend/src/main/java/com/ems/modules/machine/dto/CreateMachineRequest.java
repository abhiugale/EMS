package com.ems.modules.machine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMachineRequest {

    @NotBlank(message = "Machine name is required")
    private String name;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Machine type is required")
    private String machineType;

    @NotNull(message = "Baseline kWh is required")
    @DecimalMin(value = "0.0", message = "Baseline kWh must be positive")
    private BigDecimal baselineKwh;
}
