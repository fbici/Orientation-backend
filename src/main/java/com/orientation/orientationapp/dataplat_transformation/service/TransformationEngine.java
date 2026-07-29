package com.orientation.orientationapp.dataplat_transformation.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;

import java.util.List;
import java.util.Map;

/**
 * Engine that orchestrates data transformations.
 */
public interface TransformationEngine {

    /**
     * Apply all transformations to the data.
     *
     * @param rows    the input data
     * @param context the import context
     * @return the transformed data
     */
    List<Map<String, Object>> transform(List<Map<String, Object>> rows, ImportContext context);

    /**
     * Apply a specific transformation.
     *
     * @param rows          the input data
     * @param context       the import context
     * @param transformerId the transformer to apply
     * @return the transformed data
     */
    List<Map<String, Object>> transform(List<Map<String, Object>> rows, ImportContext context, String transformerId);
}
