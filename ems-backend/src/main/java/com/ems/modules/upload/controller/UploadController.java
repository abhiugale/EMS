package com.ems.modules.upload.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ems.common.dto.ApiResponse;
import com.ems.common.exception.UnauthorizedException;
import com.ems.modules.upload.dto.UploadResultDto;
import com.ems.modules.upload.entity.Upload;
import com.ems.modules.upload.service.UploadService;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MGR')")
    public ResponseEntity<ApiResponse<UploadResultDto>> uploadAndProcess(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mapping") String mappingJson,
            @RequestParam("timezone") String timezone,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        User user = getUser(userDetails);
        Map<String, String> columnMapping = objectMapper.readValue(mappingJson, new TypeReference<Map<String, String>>() {});

        UploadResultDto result = uploadService.uploadAndProcess(
                file,
                columnMapping,
                timezone,
                user.getFactory().getId(),
                user.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(result, "File processed successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Upload>>> getUploadHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<Upload> history = uploadService.getUploadHistory(user.getFactory().getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User session not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User profile not found"));
    }
}
