package com.ems.modules.upload;

import com.ems.modules.energy.dto.EnergyReadingDto;
import com.ems.modules.upload.service.ExcelParserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelParserServiceTest {

    private final ExcelParserService parserService = new ExcelParserService();

    @Test
    public void testParseValidExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Time");
        header.createCell(1).setCellValue("Machine");
        header.createCell(2).setCellValue("Energy");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("2026-06-07T10:00:00");
        dataRow.createCell(1).setCellValue("CNC-01");
        dataRow.createCell(2).setCellValue(123.45);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        byte[] bytes = bos.toByteArray();
        workbook.close();

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

        Map<String, String> mapping = new HashMap<>();
        mapping.put("timestamp", "Time");
        mapping.put("machine_name", "Machine");
        mapping.put("energy_kwh", "Energy");

        List<EnergyReadingDto> result = parserService.parse(file, mapping, "Asia/Kolkata");

        assertEquals(1, result.size());
        assertEquals("CNC-01", result.get(0).getMachineName());
        assertEquals(BigDecimal.valueOf(123.45), result.get(0).getEnergyKwh());
        assertNotNull(result.get(0).getRecordedAt());
    }
}
