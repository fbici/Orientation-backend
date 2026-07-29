package com.orientation.orientationapp.dataplat_parser.factory.impl;

import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_parser.factory.ParserFactory;
import com.orientation.orientationapp.dataplat_parser.strategy.FileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParserFactoryImpl implements ParserFactory {

    private final List<FileParser> parsers;
    private final Map<DataFormat, FileParser> parserCache = new ConcurrentHashMap<>();

    @Override
    public FileParser getParser(DataFormat format) {
        return Optional.ofNullable(parserCache.get(format))
                .orElseGet(() -> {
                    FileParser parser = parsers.stream()
                            .filter(p -> p.getFormat() == format)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No parser for format: " + format));
                    parserCache.put(format, parser);
                    return parser;
                });
    }

    @Override
    public FileParser detectAndParser(String fileName, String mimeType) {
        // Try by MIME type first
        if (mimeType != null) {
            for (FileParser parser : parsers) {
                for (String supported : parser.getSupportedMimeTypes()) {
                    if (supported.equals(mimeType)) {
                        return parser;
                    }
                }
            }
        }

        // Try by extension
        if (fileName != null && fileName.contains(".")) {
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            DataFormat format = DataFormat.fromExtension(ext);
            return getParser(format);
        }

        throw new IllegalArgumentException("Cannot detect format for: " + fileName);
    }

    @Override
    public boolean hasParser(DataFormat format) {
        return parsers.stream().anyMatch(p -> p.getFormat() == format);
    }
}
