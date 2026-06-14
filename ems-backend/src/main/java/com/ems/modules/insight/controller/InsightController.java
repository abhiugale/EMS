package com.ems.modules.insight.controller;

import com.ems.common.dto.ApiResponse;
import com.ems.common.exception.UnauthorizedException;
import com.ems.modules.insight.dto.InsightDto;
import com.ems.modules.insight.dto.ResolveInsightRequest;
import com.ems.modules.insight.service.InsightService;
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
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InsightDto>>> getInsights(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<InsightDto> response = insightService.getInsightsByFactory(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<InsightDto>> resolveInsight(
            @PathVariable("id") UUID insightId,
            @Valid @RequestBody ResolveInsightRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        InsightDto response = insightService.resolveInsight(insightId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Insight resolved successfully"));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User session not authenticated");
        }
        return userRepository.findByEmailWithFactory(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User profile not found"));
    }
}
