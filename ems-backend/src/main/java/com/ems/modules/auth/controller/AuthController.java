package com.ems.modules.auth.controller;

import com.ems.common.dto.ApiResponse;
import com.ems.modules.auth.dto.*;
import com.ems.modules.auth.service.AuthService;
import com.ems.modules.user.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        String registeredBy = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        UserDto response = authService.register(request, registeredBy);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
}
