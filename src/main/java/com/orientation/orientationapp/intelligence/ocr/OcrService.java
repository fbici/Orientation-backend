package com.orientation.orientationapp.intelligence.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Service OCR pour l'extraction de texte a partir de documents.
 *
 * Supporte :
 * - PDF (extraction basique)
 * - Word (.docx) via Apache POI
 * - Excel (.xlsx) via Apache POI
 * - CSV (lecture directe)
 * - Images (placeholder pour Tesseract)
 */
@Slf4j
@Service
public class OcrService {

    /**
     * Extrait le texte d'un document.
     */
    public String extractText(byte[] content, String fileName) {
        String extension = getExtension(fileName).toLowerCase();
        log.info("OCR: extracting text from {} (type={})", fileName, extension);

        try {
            return switch (extension) {
                case "pdf" -> extractFromPdf(content);
                case "jpg", "jpeg", "png", "tiff", "tif", "bmp" -> extractFromImage(content);
                case "docx" -> extractFromDocx(content);
                case "xlsx", "xls" -> extractFromExcel(content);
                case "csv" -> extractFromCsv(content);
                case "txt" -> new String(content, StandardCharsets.UTF_8);
                default -> new String(content, StandardCharsets.UTF_8);
            };
        } catch (Exception e) {
            log.error("OCR failed for {}: {}", fileName, e.getMessage());
            return "";
        }
    }

    private String extractFromPdf(byte[] content) {
        // PDF text extraction using basic string scanning
        // In production, add PDFBox dependency for proper extraction
        String raw = new String(content, StandardCharsets.ISO_8859_1);

        // Try to find text streams in PDF
        StringBuilder text = new StringBuilder();
        int idx = 0;
        while (idx < raw.length()) {
            int start = raw.indexOf("stream", idx);
            if (start == -1) break;
            int end = raw.indexOf("endstream", start);
            if (end == -1) break;
            String chunk = raw.substring(start + 6, end).trim();
            // Try to extract readable text
            for (char c : chunk.toCharArray()) {
                if (c >= 32 && c < 127) text.append(c);
            }
            text.append(" ");
            idx = end + 9;
        }

        String result = text.toString().trim();
        if (result.length() < 50) {
            // Fallback: return raw bytes as text
            result = new String(content, StandardCharsets.UTF_8);
        }
        log.info("PDF extraction: {} chars", result.length());
        return result;
    }

    private String extractFromImage(byte[] content) {
        log.info("Image OCR: {} bytes (Tesseract required for production)", content.length);
        return "[Image content - OCR extraction required]";
    }

    private String extractFromDocx(byte[] content) throws Exception {
        try (var doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(content))) {
            StringBuilder sb = new StringBuilder();
            for (var paragraph : doc.getParagraphs()) {
                sb.append(paragraph.getText()).append("\n");
            }
            for (var table : doc.getTables()) {
                for (var row : table.getRows()) {
                    for (var cell : row.getTableCells()) {
                        sb.append(cell.getText()).append("\t");
                    }
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }

    private String extractFromExcel(byte[] content) throws Exception {
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(content))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var sheet = workbook.getSheetAt(i);
                sb.append("=== ").append(sheet.getSheetName()).append(" ===\n");
                for (var row : sheet) {
                    for (var cell : row) {
                        sb.append(cell.toString()).append("\t");
                    }
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }

    private String extractFromCsv(byte[] content) {
        return new String(content, StandardCharsets.UTF_8);
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }
}
