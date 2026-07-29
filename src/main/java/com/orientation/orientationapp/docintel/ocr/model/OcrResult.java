package com.orientation.orientationapp.docintel.ocr.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrResult {

    private String rawText;
    private String cleanedText;
    private double confidence;
    private String detectedLanguage;
    private int pageCount;
    private List<OcrPage> pages;
    private List<OcrBlock> blocks;
    private List<String> paragraphs;
    private List<String> titles;
    private List<OcrTable> tables;
    private List<OcrImage> images;
    private String engineUsed;
    private long processingTimeMs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OcrPage {
        private int pageNumber;
        private String text;
        private double confidence;
        private int width;
        private int height;
        private List<OcrBlock> blocks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OcrBlock {
        private String type;
        private String text;
        private double confidence;
        private int x;
        private int y;
        private int width;
        private int height;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OcrTable {
        private int rows;
        private int columns;
        private List<List<String>> cells;
        private double confidence;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OcrImage {
        private int pageNumber;
        private int x;
        private int y;
        private int width;
        private int height;
        private String description;
    }
}
