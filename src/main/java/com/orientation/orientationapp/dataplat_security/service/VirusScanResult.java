package com.orientation.orientationapp.dataplat_security.service;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirusScanResult {

    private boolean clean;
    private String virusName;
    private String scanEngine;
    private long scanDurationMs;

    public static VirusScanResult clean(String engine, long durationMs) {
        return VirusScanResult.builder()
                .clean(true)
                .scanEngine(engine)
                .scanDurationMs(durationMs)
                .build();
    }

    public static VirusScanResult infected(String virusName, String engine) {
        return VirusScanResult.builder()
                .clean(false)
                .virusName(virusName)
                .scanEngine(engine)
                .build();
    }
}
