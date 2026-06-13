package com.ems.modules.alert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveAlertRequest {

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
}
