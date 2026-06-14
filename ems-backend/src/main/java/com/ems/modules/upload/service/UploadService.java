package com.ems.modules.upload.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.common.exception.ValidationException;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import com.ems.modules.upload.dto.UploadResultDto;
import com.ems.modules.upload.entity.Upload;
import com.ems.modules.upload.repository.UploadRepository;
import com.ems.modules.energy.dto.EnergyReadingDto;
import com.ems.modules.insight.service.MlClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final UploadRepository uploadRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;
    private final ExcelParserService excelParserService;
    private final EnergyReadingIngester energyReadingIngester;
    private final MlClientService mlClientService;

    /** Reads only the header row + detects format type. Returns {headers, format}. */
    public Map<String, Object> extractHeadersWithFormat(MultipartFile file) {
        return excelParserService.extractHeadersWithFormat(file);
    }

    @Transactional
    public UploadResultDto uploadAndProcess(
            MultipartFile file,
            Map<String, String> columnMapping,
            String timezone,
            String formatType,
            UUID factoryId,
            UUID userId) {

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Upload upload = Upload.builder()
                .factory(factory)
                .uploadedBy(user)
                .filename(file.getOriginalFilename())
                .originalName(file.getOriginalFilename())
                .status("PROCESSING")
                .factoryTimezone(timezone)
                .build();

        upload = uploadRepository.save(upload);

        try {
            List<EnergyReadingDto> readings;
            if (ExcelParserService.FORMAT_NARROW.equalsIgnoreCase(formatType)) {
                readings = excelParserService.parseNarrow(file, columnMapping, timezone);
            } else {
                readings = excelParserService.parse(file, columnMapping, timezone);
            }

            energyReadingIngester.ingest(readings, upload.getId());

            upload.setStatus("SUCCESS");
            upload.setRowCount(readings.size());
            upload.setProcessedAt(Instant.now());
            uploadRepository.save(upload);

            mlClientService.triggerBatchPrediction(upload.getId());

            return UploadResultDto.builder()
                    .uploadId(upload.getId())
                    .rowsProcessed(readings.size())
                    .rowsSkipped(0)
                    .duplicates(0)
                    .status("SUCCESS")
                    .build();

        } catch (ValidationException e) {
            // 400 — user error (bad mapping, empty file, missing columns)
            log.warn("Validation error during upload '{}': {}", file.getOriginalFilename(), e.getMessage());
            upload.setStatus("FAILED");
            upload.setErrorMessage(e.getMessage());
            upload.setProcessedAt(Instant.now());
            uploadRepository.save(upload);
            throw e;   // Let GlobalExceptionHandler return 400, not 500

        } catch (Exception e) {
            // 500 — unexpected error
            log.error("Upload ingestion failed for uploadId={} file='{}': {}",
                    upload.getId(), file.getOriginalFilename(), e.getMessage(), e);
            upload.setStatus("FAILED");
            upload.setErrorMessage(e.getMessage());
            upload.setProcessedAt(Instant.now());
            uploadRepository.save(upload);
            throw new RuntimeException("Excel ingestion failed: " + e.getMessage(), e);
        }
    }

    public List<Upload> getUploadHistory(UUID factoryId) {
        return uploadRepository.findByFactoryIdOrderByCreatedAtDesc(factoryId);
    }
}
