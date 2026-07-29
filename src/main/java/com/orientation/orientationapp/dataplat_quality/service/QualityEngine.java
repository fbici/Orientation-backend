package com.orientation.orientationapp.dataplat_quality.service;

import com.orientation.orientationapp.dataplat_formats.core.model.QualityReport;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;
import java.util.Map;

/**
 * Engine that produces quality reports for imported data.
 */
public interface QualityEngine {

    /**
     * Generate a quality report for the given data.
     *
     * @param rows     the data rows
     * @param dataType the data type
     * @return the quality report
     */
    QualityReport analyze(List<Map<String, Object>> rows, DataType dataType);

    /**
     * Compare quality between two versions.
     *
     * @param oldRows  the old version data
     * @param newRows  the new version data
     * @param dataType the data type
     * @return quality comparison report
     */
    QualityReport compareQuality(List<Map<String, Object>> oldRows, List<Map<String, Object>> newRows, DataType dataType);
}
