package com.ems.modules.insight.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MlClientService {
    private static final Logger log = LoggerFactory.getLogger(MlClientService.class);

    private final WebClient mlWebClient;

    @Async
    public void triggerBatchPrediction(UUID uploadId) {
        log.info("Asynchronously triggering ML batch prediction for upload: {}", uploadId);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("upload_id", uploadId.toString());

        mlWebClient.post()
                .uri("/predict/batch")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("ML service is offline or returned error for upload {}. Falling back gracefully. Error: {}", uploadId, e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }
}
