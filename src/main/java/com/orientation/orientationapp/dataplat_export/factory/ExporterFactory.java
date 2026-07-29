package com.orientation.orientationapp.dataplat_export.factory;

import com.orientation.orientationapp.dataplat_export.strategy.DataExporter;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;

/**
 * Factory for creating data exporters.
 */
public interface ExporterFactory {

    /**
     * Get the exporter for a specific format.
     *
     * @param format the export format
     * @return the exporter
     */
    DataExporter getExporter(DataFormat format);

    /**
     * Get all exporters that support a specific data type.
     *
     * @param dataType the data type
     * @return list of compatible exporters
     */
    List<DataExporter> getExporters(DataType dataType);
}
