package com.ems.modules.report;

import com.ems.infrastructure.storage.MinioService;
import com.ems.modules.energy.dto.BreakdownDto;
import com.ems.modules.energy.dto.SummaryDto;
import com.ems.modules.energy.service.EnergyService;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import com.ems.modules.report.entity.Report;
import com.ems.modules.report.repository.ReportRepository;
import com.ems.modules.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private FactoryRepository factoryRepository;

    @Mock
    private EnergyService energyService;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private ReportService reportService;

    private UUID reportId;
    private Report report;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        reportId = UUID.randomUUID();
        Factory factory = Factory.builder()
                .id(UUID.randomUUID())
                .name("Test Plant")
                .timezone("Asia/Kolkata")
                .tariffInrPerKwh(BigDecimal.valueOf(8.0))
                .build();
        report = Report.builder()
                .id(reportId)
                .factory(factory)
                .reportType("DAILY")
                .status("PROCESSING")
                .build();
    }

    @Test
    public void testGeneratePdfReportAsync_Success() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(energyService.getSummary(any(), any())).thenReturn(
                SummaryDto.builder()
                        .totalKwh(BigDecimal.valueOf(1000))
                        .totalCost(BigDecimal.valueOf(8000))
                        .averagePowerFactor(BigDecimal.valueOf(0.95))
                        .hourlyCurve(Collections.emptyList())
                        .build()
        );
        when(energyService.getBreakdown(any(), eq("department"), any(), any())).thenReturn(
                BreakdownDto.builder().items(Collections.emptyList()).build()
        );

        reportService.generatePdfReportAsync(reportId, LocalDate.now());

        verify(minioService, times(1)).uploadFile(anyString(), any(InputStream.class), eq("application/pdf"), anyLong());
        verify(reportRepository, times(1)).save(argThat(r -> "SUCCESS".equals(r.getStatus())));
    }
}
