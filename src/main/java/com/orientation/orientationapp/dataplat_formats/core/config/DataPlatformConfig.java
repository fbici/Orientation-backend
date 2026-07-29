package com.orientation.orientationapp.dataplat_formats.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.dataplatform")
public class DataPlatformConfig {

    private long maxFileSize = 50 * 1024 * 1024; // 50MB default
    private List<String> allowedMimeTypes = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv",
            "application/json",
            "application/xml"
    );
    private int maxRowsPerImport = 100000;
    private int batchSize = 1000;
    private boolean asyncEnabled = true;
    private int threadPoolSize = 10;
    private boolean virusScanEnabled = false;
    private boolean integrityCheckEnabled = true;
    private String tempDir = "/tmp/dataplatform";

    // Quality thresholds
    private double qualityWarningThreshold = 80.0;
    private double qualityErrorThreshold = 60.0;

    // Version management
    private int maxVersionsPerCampaign = 50;
    private boolean autoActivateVersion = false;
}
