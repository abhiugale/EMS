package com.ems.modules.report.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.modules.energy.dto.BreakdownDto;
import com.ems.modules.energy.dto.SummaryDto;
import com.ems.modules.energy.service.EnergyService;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import com.ems.modules.report.dto.ReportDto;
import com.ems.modules.report.entity.Report;
import com.ems.modules.report.repository.ReportRepository;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import com.ems.infrastructure.storage.MinioService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository reportRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;
    private final EnergyService energyService;
    private final MinioService minioService;

    @Transactional
    public ReportDto triggerReportGeneration(UUID factoryId, UUID userId, LocalDate date, String type) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Report report = Report.builder()
                .factory(factory)
                .generatedBy(user)
                .reportType(type)
                .status("PROCESSING")
                .build();

        Report saved = reportRepository.save(report);

        // Trigger PDF generation in background
        generatePdfReportAsync(saved.getId(), date);

        return mapToDto(saved);
    }

    @Async
    @Transactional
    public void generatePdfReportAsync(UUID reportId, LocalDate date) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report record not found"));

        try {
            UUID factoryId = report.getFactory().getId();
            SummaryDto summary = energyService.getSummary(factoryId, date);
            BreakdownDto breakdown = energyService.getBreakdown(factoryId, "department", 
                    date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(),
                    date.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Document Styling
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("Energy Management System (EMS) Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph info = new Paragraph("Factory: " + report.getFactory().getName() + " | Date: " + date, subTitleFont);
            info.setAlignment(Element.ALIGN_CENTER);
            info.setSpacingAfter(25);
            document.add(info);

            // Energy Consumption Summary Section
            Paragraph section1 = new Paragraph("1. Energy Consumption Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            section1.setSpacingAfter(10);
            document.add(section1);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.addCell("Total Consumption (kWh)");
            summaryTable.addCell(summary.getTotalKwh() != null ? summary.getTotalKwh().toString() : "0.0");
            summaryTable.addCell("Total Cost (INR)");
            summaryTable.addCell(summary.getTotalCost() != null ? summary.getTotalCost().toString() : "0.0");
            summaryTable.addCell("Average Power Factor");
            summaryTable.addCell(summary.getAveragePowerFactor() != null ? summary.getAveragePowerFactor().toString() : "1.0");
            summaryTable.setSpacingAfter(20);
            document.add(summaryTable);

            // Department Breakdown Section
            Paragraph section2 = new Paragraph("2. Department-wise Energy Breakdown", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            section2.setSpacingAfter(10);
            document.add(section2);

            PdfPTable bdTable = new PdfPTable(4);
            bdTable.setWidthPercentage(100);
            bdTable.addCell("Department");
            bdTable.addCell("Consumption (kWh)");
            bdTable.addCell("Cost (INR)");
            bdTable.addCell("Share (%)");

            if (breakdown.getItems() != null) {
                for (BreakdownDto.BreakdownItem item : breakdown.getItems()) {
                    bdTable.addCell(item.getName());
                    bdTable.addCell(item.getKwh() != null ? item.getKwh().toString() : "0.0");
                    bdTable.addCell(item.getCost() != null ? item.getCost().toString() : "0.0");
                    bdTable.addCell(item.getPercentage() != null ? item.getPercentage().toString() : "0.0");
                }
            }
            bdTable.setSpacingAfter(20);
            document.add(bdTable);

            document.close();

            byte[] pdfBytes = out.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes);

            String objectName = "reports/" + factoryId + "/" + reportId + ".pdf";
            minioService.uploadFile(objectName, bis, "application/pdf", pdfBytes.length);

            // Finalize execution
            report.setStatus("SUCCESS");
            report.setFilePath(objectName);
            report.setCompletedAt(Instant.now());
            reportRepository.save(report);

        } catch (Exception e) {
            log.error("Failed to generate PDF for report {}. Error: {}", reportId, e.getMessage(), e);
            report.setStatus("FAILED");
            report.setCompletedAt(Instant.now());
            reportRepository.save(report);
        }
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByFactory(UUID factoryId) {
        return reportRepository.findByFactoryIdOrderByCreatedAtDesc(factoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ReportDto mapToDto(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .factoryId(report.getFactory().getId())
                .generatedByEmail(report.getGeneratedBy() != null ? report.getGeneratedBy().getEmail() : null)
                .reportType(report.getReportType())
                .status(report.getStatus())
                .filePath(report.getFilePath())
                .createdAt(report.getCreatedAt())
                .completedAt(report.getCompletedAt())
                .build();
    }
}
