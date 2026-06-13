package com.ems.modules.alert.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.modules.alert.dto.AlertDto;
import com.ems.modules.alert.dto.ResolveAlertRequest;
import com.ems.modules.alert.entity.Alert;
import com.ems.modules.alert.repository.AlertRepository;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AlertDto createAlert(Alert alert) {
        Alert saved = alertRepository.save(alert);
        AlertDto dto = mapToDto(saved);

        // Broadcast event over WebSocket
        messagingTemplate.convertAndSend("/topic/alerts/" + alert.getFactory().getId(), dto);

        // Async dispatch alert notification emails
        notificationService.sendEmail("admin@ems.local", "CRITICAL EMS ALERT: " + alert.getAlertType(), alert.getMessage());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<AlertDto> getAlertsByFactory(UUID factoryId) {
        return alertRepository.findByFactoryIdOrderByCreatedAtDesc(factoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertDto resolveAlert(UUID alertId, UUID userId, ResolveAlertRequest request) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        alert.setStatus("RESOLVED");
        alert.setResolvedAt(Instant.now());
        alert.setResolvedBy(user);
        alert.setMessage(alert.getMessage() + " [Resolution: " + request.getResolutionNotes() + "]");

        Alert updated = alertRepository.save(alert);
        AlertDto dto = mapToDto(updated);

        // Broadcast resolution event over WebSocket
        messagingTemplate.convertAndSend("/topic/alerts/" + alert.getFactory().getId(), dto);

        return dto;
    }

    private AlertDto mapToDto(Alert alert) {
        return AlertDto.builder()
                .id(alert.getId())
                .factoryId(alert.getFactory().getId())
                .machineId(alert.getMachine() != null ? alert.getMachine().getId() : null)
                .machineName(alert.getMachine() != null ? alert.getMachine().getName() : "System")
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .thresholdValue(alert.getThresholdValue())
                .actualValue(alert.getActualValue())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByEmail(alert.getResolvedBy() != null ? alert.getResolvedBy().getEmail() : null)
                .build();
    }
}
