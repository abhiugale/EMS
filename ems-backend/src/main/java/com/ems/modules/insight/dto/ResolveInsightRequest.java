package com.ems.modules.insight.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveInsightRequest {

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
}
