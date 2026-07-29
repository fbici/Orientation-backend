package com.orientation.orientationapp.dataplat_import.service.impl;

import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_import.service.EntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EntityMapperRegistry {

    private final Map<DataType, EntityMapper> registry = new ConcurrentHashMap<>();

    public EntityMapperRegistry(List<EntityMapper> mappers) {
        for (EntityMapper mapper : mappers) {
            registry.put(mapper.getDataType(), mapper);
            log.info("Registered entity mapper for: {}", mapper.getDataType());
        }
    }

    public Optional<EntityMapper> getMapper(DataType dataType) {
        return Optional.ofNullable(registry.get(dataType));
    }

    public boolean hasMapper(DataType dataType) {
        return registry.containsKey(dataType);
    }
}
