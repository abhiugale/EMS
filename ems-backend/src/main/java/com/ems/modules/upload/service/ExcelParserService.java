package com.ems.modules.upload.service;

import com.ems.common.exception.ValidationException;
import com.ems.modules.energy.dto.EnergyReadingDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelParserService {

    public List<EnergyReadingDto> parse(MultipartFile file, Map<String, String> columnMapping, String timezoneStr) {
        List<EnergyReadingDto> readings = new ArrayList<>();
        ZoneId sourceZoneId = ZoneId.of(timezoneStr != null ? timezoneStr : "Asia/Kolkata");

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new ValidationException("Excel file is empty");
            }

            // Parse Headers
            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
                }
            }

            // Resolve target indexes based on mapping
            // columnMapping: systemField -> excelHeaderName
            Integer timestampCol = getColumnIndex(columnMapping.get("timestamp"), headerMap);
            Integer machineNameCol = getColumnIndex(columnMapping.get("machine_name"), headerMap);
            Integer energyKwhCol = getColumnIndex(columnMapping.get("energy_kwh"), headerMap);

            if (timestampCol == null || machineNameCol == null || energyKwhCol == null) {
                throw new ValidationException("Required columns (timestamp, machine name, energy kWh) are missing or incorrectly mapped.");
            }

            Integer activeKwCol = getColumnIndex(columnMapping.get("active_kw"), headerMap);
            Integer apparentKvaCol = getColumnIndex(columnMapping.get("apparent_kva"), headerMap);
            Integer reactiveKvarCol = getColumnIndex(columnMapping.get("reactive_kvar"), headerMap);
            Integer powerFactorCol = getColumnIndex(columnMapping.get("power_factor"), headerMap);
            Integer frequencyCol = getColumnIndex(columnMapping.get("frequency"), headerMap);
            Integer voltageRCol = getColumnIndex(columnMapping.get("voltage_r"), headerMap);
            Integer voltageYCol = getColumnIndex(columnMapping.get("voltage_y"), headerMap);
            Integer voltageBCol = getColumnIndex(columnMapping.get("voltage_b"), headerMap);
            Integer currentRCol = getColumnIndex(columnMapping.get("current_r"), headerMap);
            Integer currentYCol = getColumnIndex(columnMapping.get("current_y"), headerMap);
            Integer currentBCol = getColumnIndex(columnMapping.get("current_b"), headerMap);
            Integer partsProducedCol = getColumnIndex(columnMapping.get("parts_produced"), headerMap);

            Set<String> localDuplicateCheck = new HashSet<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }

                // Extract Machine Name
                Cell machineCell = row.getCell(machineNameCol);
                String machineName = getCellStringValue(machineCell);
                if (machineName == null || machineName.isEmpty()) {
                    continue; // Skip invalid rows
                }

                // Extract Timestamp
                Cell timeCell = row.getCell(timestampCol);
                Instant recordedAt = getCellInstantValue(timeCell, sourceZoneId);
                if (recordedAt == null) {
                    continue; // Skip invalid rows
                }

                // Skip local duplicates in the sheet itself
                String dupKey = machineName + "_" + recordedAt.toEpochMilli();
                if (localDuplicateCheck.contains(dupKey)) {
                    continue;
                }
                localDuplicateCheck.add(dupKey);

                // Extract Energy (kWh)
                BigDecimal energyKwh = getCellNumericValue(row.getCell(energyKwhCol));

                EnergyReadingDto dto = EnergyReadingDto.builder()
                        .machineName(machineName)
                        .recordedAt(recordedAt)
                        .energyKwh(energyKwh)
                        .activeKw(getCellNumericValue(row.getCell(activeKwCol)))
                        .apparentKva(getCellNumericValue(row.getCell(apparentKvaCol)))
                        .reactiveKvar(getCellNumericValue(row.getCell(reactiveKvarCol)))
                        .powerFactor(getCellNumericValue(row.getCell(powerFactorCol)))
                        .frequency(getCellNumericValue(row.getCell(frequencyCol)))
                        .voltageR(getCellNumericValue(row.getCell(voltageRCol)))
                        .voltageY(getCellNumericValue(row.getCell(voltageYCol)))
                        .voltageB(getCellNumericValue(row.getCell(voltageBCol)))
                        .currentR(getCellNumericValue(row.getCell(currentRCol)))
                        .currentY(getCellNumericValue(row.getCell(currentYCol)))
                        .currentB(getCellNumericValue(row.getCell(currentBCol)))
                        .partsProduced(getCellIntegerValue(row.getCell(partsProducedCol)))
                        .source("excel")
                        .build();

                readings.add(dto);
            }

        } catch (Exception e) {
            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            }
            throw new RuntimeException("Error parsing Excel workbook: " + e.getMessage(), e);
        }

        return readings;
    }

    private Integer getColumnIndex(String headerName, Map<String, Integer> headerMap) {
        if (headerName == null) return null;
        return headerMap.get(headerName.trim());
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue()).trim();
        }
        return null;
    }

    private BigDecimal getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Instant getCellInstantValue(Cell cell, ZoneId zoneId) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date d = cell.getDateCellValue();
                return d.toInstant();
            }
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                String val = cell.getStringCellValue().trim();
                LocalDateTime ldt = LocalDateTime.parse(val, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
                return zdt.toInstant();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
