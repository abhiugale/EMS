package com.ems.modules.machine.controller;

import com.ems.common.dto.ApiResponse;
import com.ems.common.exception.UnauthorizedException;
import com.ems.modules.machine.dto.*;
import com.ems.modules.machine.service.MachineService;
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
@RequestMapping("/api/v1/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MachineDto>>> getMachines(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<MachineDto> response = machineService.getMachinesByFactory(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<MachineDto>> createMachine(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateMachineRequest request) {
        User user = getUser(userDetails);
        MachineDto response = machineService.createMachine(user.getFactory().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Machine created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineDto>> getMachineById(
            @PathVariable("id") UUID id) {
        MachineDto response = machineService.getMachineById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<MachineDto>> updateMachine(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CreateMachineRequest request) {
        MachineDto response = machineService.updateMachine(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Machine updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<Void>> deactivateMachine(
            @PathVariable("id") UUID id) {
        machineService.deactivateMachine(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Machine deactivated successfully"));
    }

    @GetMapping("/cards")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<MachineCardDto>>> getMachineCards(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<MachineCardDto> response = machineService.getMachineCards(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User session not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User profile not found"));
    }
}
