package com.orientation.orientationapp.ai.export.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResult {

    private String format;
    private byte[] data;
    private String fileName;
    private long fileSize;
    private String mimeType;
}
