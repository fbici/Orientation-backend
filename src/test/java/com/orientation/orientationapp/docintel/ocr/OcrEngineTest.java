package com.orientation.orientationapp.docintel.ocr;

import com.orientation.orientationapp.docintel.ocr.engine.impl.LocalOcrEngine;
import com.orientation.orientationapp.docintel.ocr.model.OcrResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class OcrEngineTest {

    private LocalOcrEngine ocrEngine;

    @BeforeEach
    void setUp() {
        ocrEngine = new LocalOcrEngine();
    }

    @Test
    void shouldPerformOcrOnTextContent() {
        String text = "Guide d'orientation 2025\nUniversité Hassan II\nFaculté des Sciences";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        OcrResult result = ocrEngine.performOcr(inputStream, "text/plain");

        assertNotNull(result);
        assertNotNull(result.getRawText());
        assertTrue(result.getConfidence() > 0);
        assertEquals(1, result.getPageCount());
        assertNotNull(result.getPages());
        assertFalse(result.getPages().isEmpty());
    }

    @Test
    void shouldExtractTitles() {
        String text = "GUIDE D'ORIENTATION\nUniversité de Test\nFACULTÉ DES SCIENCES";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        OcrResult result = ocrEngine.performOcr(inputStream, "text/plain");

        assertNotNull(result.getTitles());
        assertFalse(result.getTitles().isEmpty());
    }

    @Test
    void shouldReturnCorrectEngineId() {
        assertEquals("LOCAL", ocrEngine.getEngineId());
    }

    @Test
    void shouldSupportTextMimeTypes() {
        assertTrue(ocrEngine.supportsMimeType("text/plain"));
        assertTrue(ocrEngine.supportsMimeType("text/csv"));
        assertTrue(ocrEngine.supportsMimeType("application/json"));
        assertFalse(ocrEngine.supportsMimeType("application/pdf"));
    }

    @Test
    void shouldReturnConfidenceThreshold() {
        assertTrue(ocrEngine.getConfidenceThreshold() > 0);
    }
}
