package com.ems.modules.energy.controller;

import com.ems.common.dto.ApiResponse;
import com.ems.common.exception.UnauthorizedException;
import com.ems.modules.dashboard.dto.*;
import com.ems.modules.energy.dto.*;
import com.ems.modules.energy.service.EnergyService;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/energy")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyService energyService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SummaryDto>> getSummary(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        SummaryDto result = energyService.getSummary(user.getFactory().getId(), targetDate);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/breakdown")
    public ResponseEntity<ApiResponse<BreakdownDto>> getBreakdown(
            @RequestParam("groupBy") String groupBy,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        BreakdownDto result = energyService.getBreakdown(user.getFactory().getId(), groupBy, from, to);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/ceo-summary")
    public ResponseEntity<ApiResponse<CeoSummaryDto>> getCeoSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        CeoSummaryDto result = energyService.getCeoSummary(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/peak-demand")
    public ResponseEntity<ApiResponse<PeakDemandDto>> getPeakDemand(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        PeakDemandDto result = energyService.getPeakDemand(user.getFactory().getId(), targetDate);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/realtime")
    public ResponseEntity<ApiResponse<RealtimeDto>> getRealtime(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        RealtimeDto result = energyService.getRealtime(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User session not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User profile not found"));
    }
}
