package com.orientation.orientationapp.dataplat_parser.strategy;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Strategy interface for parsing files of different formats.
 * Each parser implementation handles a specific file format.
 */
public interface FileParser {

    /**
     * @return the format this parser handles
     */
    DataFormat getFormat();

    /**
     * Parse a file into structured rows.
     *
     * @param inputStream the file content
     * @param context     the import context
     * @return list of rows, each row is a map of column name -> value
     */
    List<Map<String, Object>> parse(InputStream inputStream, ImportContext context);

    /**
     * Extract headers/column names from the file.
     *
     * @param inputStream the file content
     * @return list of column names
     */
    List<String> extractHeaders(InputStream inputStream);

    /**
     * Validate that the file format is correct before parsing.
     *
     * @param inputStream the file content
     * @return true if the file is valid for this parser
     */
    boolean canParse(InputStream inputStream);

    /**
     * @return the MIME types this parser supports
     */
    String[] getSupportedMimeTypes();
}
