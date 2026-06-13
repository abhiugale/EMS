package com.ems.modules.report.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private UUID id;
    private UUID factoryId;
    private String generatedByEmail;
    private String reportType;
    private String status;
    private String filePath;
    private Instant createdAt;
    private Instant completedAt;
}
