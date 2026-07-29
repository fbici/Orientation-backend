package com.orientation.orientationapp.dataplat_formats.enums;

import lombok.Getter;

@Getter
public enum FileFormat {
    PDF("application/pdf", "Portable Document Format"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel Spreadsheet"),
    CSV("text/csv", "Comma-Separated Values"),
    JSON("application/json", "JavaScript Object Notation"),
    XML("application/xml", "Extensible Markup API"),
    API("application/json", "External API Response");

    private final String mimeType;
    private final String description;

    FileFormat(String mimeType, String description) {
        this.mimeType = mimeType;
        this.description = description;
    }

    public static FileFormat fromMimeType(String mimeType) {
        for (FileFormat format : values()) {
            if (format.mimeType.equals(mimeType)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown MIME type: " + mimeType);
    }

    public static FileFormat fromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> PDF;
            case "xlsx", "xls" -> EXCEL;
            case "csv" -> CSV;
            case "json" -> JSON;
            case "xml" -> XML;
            default -> throw new IllegalArgumentException("Unknown extension: " + extension);
        };
    }
}
