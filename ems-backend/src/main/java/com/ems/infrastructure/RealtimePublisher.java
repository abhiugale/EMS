package com.ems.infrastructure;

import com.ems.modules.dashboard.dto.RealtimeDto;
import com.ems.modules.energy.service.EnergyService;
import com.ems.modules.factory.repository.FactoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Pushes the latest aggregated realtime telemetry to all connected
 * WebSocket subscribers on /topic/realtime every 5 seconds.
 *
 * The payload is the same RealtimeDto that the REST endpoint returns,
 * so the frontend can use either REST polling or WebSocket — or both.
 */
@Component
@RequiredArgsConstructor
public class RealtimePublisher {

    private static final Logger log = LoggerFactory.getLogger(RealtimePublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final EnergyService energyService;
    private final FactoryRepository factoryRepository;

    @Scheduled(fixedDelay = 5000)   // every 5 seconds
    public void broadcastRealtime() {
        try {
            // Broadcast for every factory so all connected dashboards get their data
            factoryRepository.findAll().forEach(factory -> {
                try {
                    RealtimeDto dto = energyService.getRealtime(factory.getId());
                    // Topic: /topic/realtime/{factoryId}
                    messagingTemplate.convertAndSend(
                            "/topic/realtime/" + factory.getId(), dto);
                } catch (Exception e) {
                    log.debug("Could not publish realtime for factory {}: {}",
                            factory.getId(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.debug("Realtime broadcast skipped: {}", e.getMessage());
        }
    }
}
