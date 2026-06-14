package com.ems.modules.report.controller;

import com.ems.common.dto.ApiResponse;
import com.ems.common.exception.UnauthorizedException;
import com.ems.modules.report.dto.ReportDto;
import com.ems.modules.report.service.ReportService;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<ReportDto>> triggerReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("reportType") String reportType,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        ReportDto response = reportService.triggerReportGeneration(
                user.getFactory().getId(),
                user.getId(),
                date,
                reportType
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Report generation triggered successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportDto>>> getReports(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<ReportDto> response = reportService.getReportsByFactory(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User session not authenticated");
        }
        return userRepository.findByEmailWithFactory(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User profile not found"));
    }
}
