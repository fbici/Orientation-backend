package com.orientation.orientationapp.dataplat_parser.factory;

import com.orientation.orientationapp.dataplat_parser.strategy.FileParser;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;

/**
 * Factory for creating the appropriate parser based on file format.
 */
public interface ParserFactory {

    /**
     * Get the parser for a specific format.
     *
     * @param format the file format
     * @return the parser instance
     * @throws IllegalArgumentException if no parser is available for the format
     */
    FileParser getParser(DataFormat format);

    /**
     * Auto-detect format and return the appropriate parser.
     *
     * @param fileName the file name
     * @param mimeType the MIME type
     * @return the parser instance
     */
    FileParser detectAndParser(String fileName, String mimeType);

    /**
     * Check if a parser is available for the given format.
     *
     * @param format the file format
     * @return true if a parser exists
     */
    boolean hasParser(DataFormat format);
}
