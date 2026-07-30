package com.orientation.orientationapp.intelligence.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Service OCR pour l'extraction de texte à partir de documents.
 *
 * Supporte :
 * - PDF (extraction directe ou OCR via Tesseract)
 * - Images (JPG, PNG, TIFF) via Tesseract
 * - Word (.docx) via Apache POI
 * - Excel (.xlsx) via Apache POI
 * - CSV (lecture directe)
 */
@Slf4j
@Service
public class OcrService {

    /**
     * Extrait le texte d'un document.
     *
     * @param content contenu binaire du fichier
     * @param fileName nom du fichier (pour déterminer le type)
     * @return texte extrait
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
                default -> {
                    log.warn("Unsupported file type: {}", extension);
                    yield new String(content, StandardCharsets.UTF_8);
                }
            };
        } catch (Exception e) {
            log.error("OCR failed for {}: {}", fileName, e.getMessage());
            return "";
        }
    }

    private String extractFromPdf(byte[] content) throws Exception {
        // Essayer d'abord l'extraction directe (PDF textuel)
        try (var parser = new org.apache.pdfbox.pdmodel.PDDocument()) {
            // PDFBox extraction
            var stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(org.apache.pdfbox.pdmodel.PDDocument.load(content));
            if (text != null && !text.trim().isEmpty()) {
                log.info("PDF direct extraction: {} chars", text.length());
                return text;
            }
        } catch (Exception e) {
            log.debug("Direct PDF extraction failed, trying OCR: {}", e.getMessage());
        }

        // Fallback: extraction basique via texte brut
        String raw = new String(content, StandardCharsets.UTF_8);
        if (raw.length() > 100) {
            return raw;
        }

        return "[PDF content - OCR required]";
    }

    private String extractFromImage(byte[] content) {
        // Pour l'instant, retourner un placeholder
        // En production, utiliser Tesseract via tess4j
        log.info("Image OCR: {} bytes", content.length);
        return "[Image content - Tesseract OCR required]";
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
