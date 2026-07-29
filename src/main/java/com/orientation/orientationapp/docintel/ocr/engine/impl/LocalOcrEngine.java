package com.orientation.orientationapp.docintel.ocr.engine.impl;

import com.orientation.orientationapp.docintel.ocr.engine.OcrEngine;
import com.orientation.orientationapp.docintel.ocr.model.OcrResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Local OCR engine implementation.
 * Provides basic text extraction without external dependencies.
 * In production, this would be replaced by Tesseract or similar.
 */
@Slf4j
@Component
public class LocalOcrEngine implements OcrEngine {

    @Override
    public String getEngineId() {
        return "LOCAL";
    }

    @Override
    public String getEngineName() {
        return "Local OCR Engine";
    }

    @Override
    public OcrResult performOcr(InputStream inputStream, String mimeType) {
        log.info("Performing OCR with Local engine for mimeType: {}", mimeType);

        long startTime = System.currentTimeMillis();

        // For text-based formats, extract directly
        String rawText = extractTextFromStream(inputStream, mimeType);

        long processingTime = System.currentTimeMillis() - startTime;

        return OcrResult.builder()
                .rawText(rawText)
                .cleanedText(cleanText(rawText))
                .confidence(0.85)
                .detectedLanguage("fr")
                .pageCount(1)
                .pages(List.of(OcrResult.OcrPage.builder()
                        .pageNumber(1)
                        .text(rawText)
                        .confidence(0.85)
                        .build()))
                .blocks(extractBlocks(rawText))
                .paragraphs(extractParagraphs(rawText))
                .titles(extractTitles(rawText))
                .tables(new ArrayList<>())
                .images(new ArrayList<>())
                .engineUsed(getEngineId())
                .processingTimeMs(processingTime)
                .build();
    }

    @Override
    public boolean supportsMimeType(String mimeType) {
        return mimeType != null && (
                mimeType.equals("text/plain") ||
                mimeType.equals("text/csv") ||
                mimeType.equals("application/json") ||
                mimeType.equals("application/xml") ||
                mimeType.equals("text/xml")
        );
    }

    @Override
    public double getConfidenceThreshold() {
        return 0.7;
    }

    private String extractTextFromStream(InputStream inputStream, String mimeType) {
        try {
            return new String(inputStream.readAllBytes());
        } catch (Exception e) {
            log.error("Failed to extract text", e);
            return "";
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("\\s+", " ").trim();
    }

    private List<OcrResult.OcrBlock> extractBlocks(String text) {
        List<OcrResult.OcrBlock> blocks = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            blocks.add(OcrResult.OcrBlock.builder()
                    .type("TEXT")
                    .text(text)
                    .confidence(0.85)
                    .build());
        }
        return blocks;
    }

    private List<String> extractParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        if (text != null) {
            String[] lines = text.split("\\n\\n+");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    paragraphs.add(trimmed);
                }
            }
        }
        return paragraphs;
    }

    private List<String> extractTitles(String text) {
        List<String> titles = new ArrayList<>();
        if (text != null) {
            String[] lines = text.split("\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.length() > 0 && trimmed.length() < 100 && trimmed.equals(trimmed.toUpperCase())) {
                    titles.add(trimmed);
                }
            }
        }
        return titles;
    }
}
