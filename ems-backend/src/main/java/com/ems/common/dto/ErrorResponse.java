package com.ems.common.dto;

import java.time.Instant;
import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    private String path;
    private Map<String, String> fields;
    private Instant timestamp;
}
