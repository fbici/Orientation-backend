package com.orientation.orientationapp.dataplat_parser.strategy.impl;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.converter.RowMapper;
import com.orientation.orientationapp.dataplat_parser.strategy.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class CsvParser implements FileParser {

    private static final char[] SEPARATORS = {',', ';', '\t', '|'};

    @Override
    public DataFormat getFormat() {
        return DataFormat.CSV;
    }

    @Override
    public List<Map<String, Object>> parse(InputStream inputStream, ImportContext context) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try {
            // Read all content first to allow multiple passes
            byte[] allBytes = inputStream.readAllBytes();
            String content = new String(allBytes, StandardCharsets.UTF_8);

            char separator = detectSeparator(content);

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setDelimiter(separator)
                    .build();

            try (Reader reader = new StringReader(content);
                 CSVParser parser = new CSVParser(reader, format)) {

                List<String> headers = parser.getHeaderNames();
                log.info("CSV headers detected: {}", headers);

                for (CSVRecord record : parser) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (String header : headers) {
                        String value = record.isMapped(header) ? record.get(header) : "";
                        row.put(RowMapper.normalizeKey(header), value != null ? value.trim() : "");
                    }
                    rows.add(row);
                }

                log.info("CSV parsed: {} rows", rows.size());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file", e);
        }

        return rows;
    }

    @Override
    public List<String> extractHeaders(InputStream inputStream) {
        try {
            byte[] allBytes = inputStream.readAllBytes();
            String content = new String(allBytes, StandardCharsets.UTF_8);

            char separator = detectSeparator(content);

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setDelimiter(separator)
                    .build();

            try (Reader reader = new StringReader(content);
                 CSVParser parser = new CSVParser(reader, format)) {
                return parser.getHeaderNames();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract CSV headers", e);
        }
    }

    @Override
    public boolean canParse(InputStream inputStream) {
        try {
            byte[] header = new byte[1024];
            int read = inputStream.read(header);
            inputStream.reset();

            String content = new String(header, 0, Math.min(read, header.length), StandardCharsets.UTF_8);

            boolean hasComma = content.contains(",");
            boolean hasSemicolon = content.contains(";");
            boolean hasNewline = content.contains("\n") || content.contains("\r");

            return (hasComma || hasSemicolon) && hasNewline;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"text/csv", "application/csv", "text/plain", "application/vnd.ms-excel"};
    }

    private char detectSeparator(String content) {
        String sample = content.length() > 4096 ? content.substring(0, 4096) : content;

        for (char separator : SEPARATORS) {
            int count = 0;
            for (char c : sample.toCharArray()) {
                if (c == separator) count++;
            }
            if (count > 2) return separator;
        }

        return ','; // default
    }
}
