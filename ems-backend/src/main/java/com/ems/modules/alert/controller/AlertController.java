package com.ems.modules.alert.controller;

import com.ems.common.dto.ApiResponse;
import com.ems.common.exception.UnauthorizedException;
import com.ems.modules.alert.dto.AlertDto;
import com.ems.modules.alert.dto.ResolveAlertRequest;
import com.ems.modules.alert.service.AlertService;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDto>>> getAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<AlertDto> response = alertService.getAlertsByFactory(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<AlertDto>> resolveAlert(
            @PathVariable("id") UUID alertId,
            @Valid @RequestBody ResolveAlertRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        AlertDto response = alertService.resolveAlert(alertId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Alert resolved successfully"));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User session not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User profile not found"));
    }
}
