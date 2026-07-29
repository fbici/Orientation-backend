package com.orientation.orientationapp.dataplat_import.service;

import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for mapping imported data to JPA entities.
 */
public interface EntityMapper {

    /**
     * @return the data type this mapper handles
     */
    DataType getDataType();

    /**
     * Map a list of rows to entities and persist them.
     *
     * @param rows the data rows
     * @return the number of entities persisted
     */
    int mapAndPersist(List<Map<String, Object>> rows);

    /**
     * Get the entity class this mapper handles.
     *
     * @return the entity class
     */
    Class<?> getEntityClass();

    /**
     * Get the repository class this mapper uses.
     *
     * @return the repository class
     */
    Class<?> getRepositoryClass();
}
