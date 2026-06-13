package com.ems.modules.upload.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDto {
    private UUID uploadId;
    private Integer rowsProcessed;
    private Integer rowsSkipped;
    private Integer duplicates;
    private String status;
}
