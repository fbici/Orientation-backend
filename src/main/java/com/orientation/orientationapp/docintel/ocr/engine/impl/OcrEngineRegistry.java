package com.orientation.orientationapp.docintel.ocr.engine.impl;

import com.orientation.orientationapp.docintel.ocr.engine.OcrEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OcrEngineRegistry {

    private final Map<String, OcrEngine> engines = new ConcurrentHashMap<>();
    private final String activeEngineId;

    public OcrEngineRegistry(List<OcrEngine> engineList,
                              @Value("${app.docintel.ocr.active-engine:LOCAL}") String activeEngineId) {
        this.activeEngineId = activeEngineId;

        for (OcrEngine engine : engineList) {
            engines.put(engine.getEngineId(), engine);
            log.info("Registered OCR engine: {} ({})", engine.getEngineName(), engine.getEngineId());
        }

        log.info("Active OCR engine: {}", activeEngineId);
    }

    public OcrEngine getActiveEngine() {
        return engines.getOrDefault(activeEngineId, engines.values().iterator().next());
    }

    public Optional<OcrEngine> getEngine(String engineId) {
        return Optional.ofNullable(engines.get(engineId));
    }

    public OcrEngine getEngineForMimeType(String mimeType) {
        return engines.values().stream()
                .filter(e -> e.supportsMimeType(mimeType))
                .findFirst()
                .orElseGet(this::getActiveEngine);
    }
}
