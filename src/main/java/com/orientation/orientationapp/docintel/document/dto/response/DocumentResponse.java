package com.orientation.orientationapp.docintel.document.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private UUID id;
    private String title;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private String checksum;
    private String documentType;
    private String status;
    private String tenantId;
    private String uploadedBy;
    private Instant uploadedAt;
    private String description;
    private String language;
    private Integer pageCount;
    private BigDecimal ocrScore;
    private BigDecimal qualityScore;
    private Instant createdAt;
}
