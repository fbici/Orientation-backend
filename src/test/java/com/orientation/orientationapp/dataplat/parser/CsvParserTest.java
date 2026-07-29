package com.orientation.orientationapp.dataplat.parser;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_parser.strategy.impl.CsvParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private CsvParser csvParser;

    @BeforeEach
    void setUp() {
        csvParser = new CsvParser();
    }

    @Test
    void shouldParseCsvWithCommaSeparator() {
        String csv = "name,code,country\nUniversité Test,UT,MAR\nAutre Université,AU,FRA";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        List<Map<String, Object>> rows = csvParser.parse(inputStream, context);

        assertEquals(2, rows.size());
        assertEquals("Université Test", rows.get(0).get("name"));
        assertEquals("UT", rows.get(0).get("code"));
        assertEquals("MAR", rows.get(0).get("country"));
    }

    @Test
    void shouldParseCsvWithSemicolonSeparator() {
        String csv = "name;code;country\nUniversité Test;UT;MAR\nAutre Université;AU;FRA";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        List<Map<String, Object>> rows = csvParser.parse(inputStream, context);

        assertEquals(2, rows.size());
        assertEquals("Université Test", rows.get(0).get("name"));
    }

    @Test
    void shouldExtractHeaders() {
        String csv = "name,code,country\nUniversité Test,UT,MAR";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<String> headers = csvParser.extractHeaders(inputStream);

        assertEquals(3, headers.size());
        assertTrue(headers.contains("name"));
        assertTrue(headers.contains("code"));
        assertTrue(headers.contains("country"));
    }

    @Test
    void shouldHandleEmptyFile() {
        String csv = "";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        List<Map<String, Object>> rows = csvParser.parse(inputStream, context);

        assertTrue(rows.isEmpty());
    }

    @Test
    void shouldReturnCorrectFormat() {
        assertEquals(DataFormat.CSV, csvParser.getFormat());
    }

    @Test
    void shouldSupportCorrectMimeTypes() {
        String[] mimeTypes = csvParser.getSupportedMimeTypes();
        assertTrue(mimeTypes.length > 0);
        assertEquals("text/csv", mimeTypes[0]);
    }

    @Test
    void shouldHandleUtf8Characters() {
        String csv = "name,description\nUniversité,Faculté des Sciences\nÜniversite,Fakülte";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        List<Map<String, Object>> rows = csvParser.parse(inputStream, context);

        assertEquals(2, rows.size());
        assertEquals("Université", rows.get(0).get("name"));
        assertEquals("Üniversite", rows.get(1).get("name"));
    }
}
