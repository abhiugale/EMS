package com.ems.modules.upload.service;

import com.ems.common.exception.ValidationException;
import com.ems.modules.energy.dto.EnergyReadingDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelParserService {

    // ─────────────────────────────────────────────────────────────
    // Public constants used by the controller / frontend
    // ─────────────────────────────────────────────────────────────

    public static final String FORMAT_WIDE   = "WIDE";
    public static final String FORMAT_NARROW = "NARROW";

    // ─────────────────────────────────────────────────────────────
    // Header extraction — also detects the file format
    // ─────────────────────────────────────────────────────────────

    /** Returns just the column headers (used by preview endpoint). */
    public List<String> extractHeaders(MultipartFile file) {
        return extractHeadersWithFormat(file).get("headers") instanceof List
                ? castHeaders(extractHeadersWithFormat(file).get("headers"))
                : Collections.emptyList();
    }

    /**
     * Returns a map with two keys:
     *   "headers" → List<String>  (column names of the first row)
     *   "format"  → String        ("WIDE" or "NARROW")
     */
    public Map<String, Object> extractHeadersWithFormat(MultipartFile file) {
        List<String> headers = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return Map.of("headers", headers, "format", FORMAT_WIDE);
            }
            Row headerRow = rowIterator.next();
            for (Cell cell : headerRow) {
                if (cell == null) continue;
                if (cell.getCellType() == CellType.STRING) {
                    String val = cell.getStringCellValue().trim();
                    if (!val.isEmpty()) headers.add(val);
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    headers.add(String.valueOf((long) cell.getNumericCellValue()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read Excel headers: " + e.getMessage(), e);
        }

        String format = detectFormat(headers);
        return Map.of("headers", headers, "format", format);
    }

    /**
     * Narrow format signature: must have exactly 3–5 columns where one looks like
     * a "tag/parameter" column and one looks like a "value" column.
     * Heuristic: headers contain items matching "tag" AND "value" (case-insensitive).
     */
    private String detectFormat(List<String> headers) {
        Set<String> lower = headers.stream()
                .map(h -> h.toLowerCase().replace("_", "").replace(" ", ""))
                .collect(Collectors.toSet());

        boolean hasTagCol   = lower.stream().anyMatch(h -> h.equals("tag") || h.equals("parameter") || h.equals("measurement") || h.equals("metric"));
        boolean hasValueCol = lower.stream().anyMatch(h -> h.equals("value") || h.equals("reading") || h.equals("data"));

        return (hasTagCol && hasValueCol) ? FORMAT_NARROW : FORMAT_WIDE;
    }

    // ─────────────────────────────────────────────────────────────
    // Wide format parser (one row = one machine reading)
    // ─────────────────────────────────────────────────────────────

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

            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
                }
            }

            Integer timestampCol   = getColumnIndex(columnMapping.get("timestamp"), headerMap);
            Integer machineNameCol = getColumnIndex(columnMapping.get("machine_name"), headerMap);
            Integer energyKwhCol   = getColumnIndex(columnMapping.get("energy_kwh"), headerMap);

            if (timestampCol == null || machineNameCol == null || energyKwhCol == null) {
                throw new ValidationException(
                        "Required columns (timestamp, machine name, energy kWh) are missing or incorrectly mapped. " +
                        "If your file has Tag/Value columns, choose \"Narrow (pivot)\" format.");
            }

            Integer activeKwCol      = getColumnIndex(columnMapping.get("active_kw"), headerMap);
            Integer apparentKvaCol   = getColumnIndex(columnMapping.get("apparent_kva"), headerMap);
            Integer reactiveKvarCol  = getColumnIndex(columnMapping.get("reactive_kvar"), headerMap);
            Integer powerFactorCol   = getColumnIndex(columnMapping.get("power_factor"), headerMap);
            Integer frequencyCol     = getColumnIndex(columnMapping.get("frequency"), headerMap);
            Integer voltageRCol      = getColumnIndex(columnMapping.get("voltage_r"), headerMap);
            Integer voltageYCol      = getColumnIndex(columnMapping.get("voltage_y"), headerMap);
            Integer voltageBCol      = getColumnIndex(columnMapping.get("voltage_b"), headerMap);
            Integer currentRCol      = getColumnIndex(columnMapping.get("current_r"), headerMap);
            Integer currentYCol      = getColumnIndex(columnMapping.get("current_y"), headerMap);
            Integer currentBCol      = getColumnIndex(columnMapping.get("current_b"), headerMap);
            Integer partsProducedCol = getColumnIndex(columnMapping.get("parts_produced"), headerMap);

            Set<String> localDuplicateCheck = new HashSet<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                Cell machineCell = row.getCell(machineNameCol);
                String machineName = getCellStringValue(machineCell);
                if (machineName == null || machineName.isEmpty()) continue;

                Cell timeCell  = row.getCell(timestampCol);
                Instant recordedAt = getCellInstantValue(timeCell, sourceZoneId);
                if (recordedAt == null) continue;

                String dupKey = machineName + "_" + recordedAt.toEpochMilli();
                if (localDuplicateCheck.contains(dupKey)) continue;
                localDuplicateCheck.add(dupKey);

                EnergyReadingDto dto = EnergyReadingDto.builder()
                        .machineName(machineName)
                        .recordedAt(recordedAt)
                        .energyKwh(getCellNumericValue(row.getCell(energyKwhCol)))
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

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Excel workbook: " + e.getMessage(), e);
        }

        return readings;
    }

    // ─────────────────────────────────────────────────────────────
    // Narrow (pivot) format parser
    // Rows look like: Timestamp | Device_ID | Tag | Value
    // Groups by (timestamp, device) and pivots tag→value into a wide EnergyReadingDto
    // ─────────────────────────────────────────────────────────────

    /**
     * @param columnMapping must contain:
     *   "timestamp"    → column name of the timestamp column
     *   "machine_name" → column name of the device/machine ID column
     *   "tag_col"      → column name of the tag/parameter name column
     *   "value_col"    → column name of the numeric value column
     */
    public List<EnergyReadingDto> parseNarrow(MultipartFile file, Map<String, String> columnMapping, String timezoneStr) {

        ZoneId sourceZoneId = ZoneId.of(timezoneStr != null ? timezoneStr : "Asia/Kolkata");

        // groupKey → accumulator (mutable holder for each field)
        Map<String, NarrowAccumulator> groups = new LinkedHashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) throw new ValidationException("Excel file is empty");

            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
                }
            }

            Integer tsCol      = getColumnIndex(columnMapping.get("timestamp"), headerMap);
            Integer machineCol = getColumnIndex(columnMapping.get("machine_name"), headerMap);
            Integer tagCol     = getColumnIndex(columnMapping.get("tag_col"), headerMap);
            Integer valueCol   = getColumnIndex(columnMapping.get("value_col"), headerMap);

            if (tsCol == null || machineCol == null || tagCol == null || valueCol == null) {
                throw new ValidationException(
                        "Narrow format requires: timestamp, device/machine ID, tag name, and value columns to be mapped.");
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                String machineName = getCellStringValue(row.getCell(machineCol));
                if (machineName == null || machineName.isEmpty()) continue;

                Instant recordedAt = getCellInstantValue(row.getCell(tsCol), sourceZoneId);
                if (recordedAt == null) continue;

                String tagName = getCellStringValue(row.getCell(tagCol));
                if (tagName == null || tagName.isEmpty()) continue;

                BigDecimal value = getCellNumericValue(row.getCell(valueCol));

                String groupKey = machineName + "_" + recordedAt.toEpochMilli();
                NarrowAccumulator acc = groups.computeIfAbsent(groupKey,
                        k -> new NarrowAccumulator(machineName, recordedAt));

                applyTagValue(acc, tagName, value);
            }

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing narrow-format Excel workbook: " + e.getMessage(), e);
        }

        return groups.values().stream()
                .map(NarrowAccumulator::toDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Tag → field mapping (case-insensitive, removes separators)
    // ─────────────────────────────────────────────────────────────

    private void applyTagValue(NarrowAccumulator acc, String tagName, BigDecimal value) {
        if (value == null) return;
        // Normalise: lowercase, strip underscores/spaces/hyphens
        String t = tagName.toLowerCase()
                .replace("_", "")
                .replace(" ", "")
                .replace("-", "");

        if      (t.equals("voltager") || t.equals("vr") || t.startsWith("voltager")) acc.voltageR = value;
        else if (t.equals("voltagey") || t.equals("vy") || t.startsWith("voltagey")) acc.voltageY = value;
        else if (t.equals("voltageb") || t.equals("vb") || t.startsWith("voltageb")) acc.voltageB = value;
        else if (t.equals("currentr") || t.equals("ir") || t.equals("cr") || t.startsWith("currentr")) acc.currentR = value;
        else if (t.equals("currenty") || t.equals("iy") || t.equals("cy") || t.startsWith("currenty")) acc.currentY = value;
        else if (t.equals("currentb") || t.equals("ib") || t.equals("cb") || t.startsWith("currentb")) acc.currentB = value;
        else if (t.equals("kw")        || t.startsWith("activekw")   || t.startsWith("activepow"))     acc.activeKw = value;
        else if (t.equals("kvar")      || t.startsWith("reactivekvar") || t.startsWith("reactive"))    acc.reactiveKvar = value;
        else if (t.equals("kva")       || t.startsWith("apparent"))                                    acc.apparentKva = value;
        else if (t.equals("pf")        || t.startsWith("powerfact")  || t.equals("pfactor"))           acc.powerFactor = value;
        else if (t.startsWith("freq")  || t.equals("hz"))                                              acc.frequency = value;
        else if (t.startsWith("energy") || t.contains("kwh"))                                          acc.energyKwh = value;
        else if (t.startsWith("partcoun") || t.startsWith("parts") || t.startsWith("prod")
              || t.startsWith("output")   || t.equals("qty") || t.startsWith("partcount"))             acc.partsProduced = value.intValue();
        // Temperature, Vibration, Machine_Status → not in EMS schema, silently ignored
    }

    // ─────────────────────────────────────────────────────────────
    // Mutable accumulator used during narrow-format pivoting
    // ─────────────────────────────────────────────────────────────

    private static class NarrowAccumulator {
        final String machineName;
        final Instant recordedAt;
        BigDecimal energyKwh, activeKw, apparentKva, reactiveKvar,
                   powerFactor, frequency,
                   voltageR, voltageY, voltageB,
                   currentR, currentY, currentB;
        Integer partsProduced;

        NarrowAccumulator(String machineName, Instant recordedAt) {
            this.machineName = machineName;
            this.recordedAt  = recordedAt;
        }

        EnergyReadingDto toDto() {
            return EnergyReadingDto.builder()
                    .machineName(machineName)
                    .recordedAt(recordedAt)
                    .energyKwh(energyKwh)
                    .activeKw(activeKw)
                    .apparentKva(apparentKva)
                    .reactiveKvar(reactiveKvar)
                    .powerFactor(powerFactor)
                    .frequency(frequency)
                    .voltageR(voltageR)
                    .voltageY(voltageY)
                    .voltageB(voltageB)
                    .currentR(currentR)
                    .currentY(currentY)
                    .currentB(currentB)
                    .partsProduced(partsProduced)
                    .source("excel")
                    .build();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Shared cell-reading helpers
    // ─────────────────────────────────────────────────────────────

    private Integer getColumnIndex(String headerName, Map<String, Integer> headerMap) {
        if (headerName == null) return null;
        return headerMap.get(headerName.trim());
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue()).trim();
        return null;
    }

    private BigDecimal getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.STRING) {
            try { return new BigDecimal(cell.getStringCellValue().trim()); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Integer.parseInt(cell.getStringCellValue().trim()); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────
    // Date/time parsing — tries 11 datetime + 5 date-only formats
    // ─────────────────────────────────────────────────────────────

    private static final List<DateTimeFormatter> STRING_DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );

    private static final List<DateTimeFormatter> STRING_DATE_ONLY_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private Instant getCellInstantValue(Cell cell, ZoneId zoneId) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                LocalDateTime ldt = cell.getLocalDateTimeCellValue();
                if (ldt != null) return ZonedDateTime.of(ldt, zoneId).toInstant();
            }
        } else if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty()) return null;

            for (DateTimeFormatter fmt : STRING_DATE_FORMATTERS) {
                try { return ZonedDateTime.of(LocalDateTime.parse(val, fmt), zoneId).toInstant(); }
                catch (DateTimeParseException ignored) { }
            }
            for (DateTimeFormatter fmt : STRING_DATE_ONLY_FORMATTERS) {
                try { return ZonedDateTime.of(LocalDate.parse(val, fmt).atStartOfDay(), zoneId).toInstant(); }
                catch (DateTimeParseException ignored) { }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> castHeaders(Object obj) {
        return (List<String>) obj;
    }
}
