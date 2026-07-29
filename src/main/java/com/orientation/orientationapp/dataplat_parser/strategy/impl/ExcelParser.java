package com.orientation.orientationapp.dataplat_parser.strategy.impl;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.converter.RowMapper;
import com.orientation.orientationapp.dataplat_parser.strategy.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Slf4j
@Component
public class ExcelParser implements FileParser {

    @Override
    public DataFormat getFormat() {
        return DataFormat.EXCEL;
    }

    @Override
    public List<Map<String, Object>> parse(InputStream inputStream, ImportContext context) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                log.warn("Excel file is empty");
                return rows;
            }

            // Read headers from first row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = getCellValue(cell);
                headers.add(header != null ? RowMapper.normalizeKey(header) : "column_" + i);
            }

            log.info("Excel headers detected: {}", headers);

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> dataRow = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String value = getCellValue(cell);
                    dataRow.put(headers.get(j), value != null ? value.trim() : "");
                }
                rows.add(dataRow);
            }

            log.info("Excel parsed: {} rows", rows.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        return rows;
    }

    @Override
    public List<String> extractHeaders(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = getCellValue(cell);
                headers.add(header != null ? RowMapper.normalizeKey(header) : "column_" + i);
            }
            return headers;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract Excel headers", e);
        }
    }

    @Override
    public boolean canParse(InputStream inputStream) {
        try {
            byte[] header = new byte[8];
            int read = inputStream.read(header);
            inputStream.reset();

            // XLSX magic bytes: PK (ZIP header)
            return read >= 2 && header[0] == (byte) 0x50 && header[1] == (byte) 0x4B;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
        };
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value) && !Double.isInfinite(value)) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> "";
            default -> "";
        };
    }
}
