package com.ems.modules.insight.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.modules.insight.dto.InsightDto;
import com.ems.modules.insight.dto.ResolveInsightRequest;
import com.ems.modules.insight.entity.AiInsight;
import com.ems.modules.insight.repository.AiInsightRepository;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final AiInsightRepository aiInsightRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<InsightDto> getInsightsByFactory(UUID factoryId) {
        return aiInsightRepository.findByFactoryIdOrderByCreatedAtDesc(factoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InsightDto resolveInsight(UUID insightId, UUID userId, ResolveInsightRequest request) {
        AiInsight insight = aiInsightRepository.findById(insightId)
                .orElseThrow(() -> new ResourceNotFoundException("Insight recommendation not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        insight.setStatus("RESOLVED");
        insight.setResolvedAt(Instant.now());
        insight.setResolvedBy(user);
        insight.setResolutionNotes(request.getResolutionNotes());

        AiInsight updated = aiInsightRepository.save(insight);
        return mapToDto(updated);
    }

    private InsightDto mapToDto(AiInsight insight) {
        return InsightDto.builder()
                .id(insight.getId())
                .factoryId(insight.getFactory().getId())
                .machineId(insight.getMachine() != null ? insight.getMachine().getId() : null)
                .machineName(insight.getMachine() != null ? insight.getMachine().getName() : "System")
                .insightType(insight.getInsightType())
                .message(insight.getMessage())
                .savingsPotentialKwh(insight.getSavingsPotentialKwh())
                .savingsPotentialInr(insight.getSavingsPotentialInr())
                .status(insight.getStatus())
                .resolutionNotes(insight.getResolutionNotes())
                .createdAt(insight.getCreatedAt())
                .resolvedAt(insight.getResolvedAt())
                .resolvedByEmail(insight.getResolvedBy() != null ? insight.getResolvedBy().getEmail() : null)
                .build();
    }
}
