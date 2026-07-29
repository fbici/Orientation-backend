package com.orientation.orientationapp.dataplat_formats.enums;

import lombok.Getter;

@Getter
public enum DataFormat {
    CSV("text/csv", ".csv"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    JSON("application/json", ".json"),
    XML("application/xml", ".xml"),
    PDF("application/pdf", ".pdf"),
    API("application/json", "");

    private final String mimeType;
    private final String extension;

    DataFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public static DataFormat fromExtension(String extension) {
        return switch (extension.toLowerCase().replaceFirst("^\\.", "")) {
            case "csv" -> CSV;
            case "xlsx", "xls" -> EXCEL;
            case "json" -> JSON;
            case "xml" -> XML;
            case "pdf" -> PDF;
            default -> throw new IllegalArgumentException("Unknown extension: " + extension);
        };
    }

    public static DataFormat fromMimeType(String mimeType) {
        for (DataFormat format : values()) {
            if (format.mimeType.equals(mimeType)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown MIME type: " + mimeType);
    }
}
