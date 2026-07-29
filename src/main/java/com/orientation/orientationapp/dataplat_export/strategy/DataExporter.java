package com.orientation.orientationapp.dataplat_export.strategy;

import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for exporting data in different formats.
 */
public interface DataExporter {

    /**
     * @return the export format
     */
    DataFormat getFormat();

    /**
     * @return the data types this exporter supports
     */
    List<DataType> getSupportedDataTypes();

    /**
     * Export data to the target format.
     *
     * @param data     the data to export
     * @param dataType the type of data
     * @return the exported content as byte array
     */
    byte[] export(List<Map<String, Object>> data, DataType dataType);

    /**
     * Export data to a file path.
     *
     * @param data     the data to export
     * @param dataType the type of data
     * @param filePath the target file path
     */
    void exportToFile(List<Map<String, Object>> data, DataType dataType, String filePath);

    /**
     * @return the file extension for this format
     */
    String getFileExtension();
}
