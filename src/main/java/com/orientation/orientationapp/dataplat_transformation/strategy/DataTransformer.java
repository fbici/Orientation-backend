package com.orientation.orientationapp.dataplat_transformation.strategy;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for transforming data between formats.
 * Transformers normalize, enrich, or reshape data.
 */
public interface DataTransformer {

    /**
     * @return the transformer identifier
     */
    String getTransformerId();

    /**
     * @return the data types this transformer handles
     */
    List<String> getSupportedDataTypes();

    /**
     * Transform a list of rows.
     *
     * @param rows    the input data
     * @param context the import context
     * @return the transformed data
     */
    List<Map<String, Object>> transform(List<Map<String, Object>> rows, ImportContext context);

    /**
     * Transform a single row.
     *
     * @param row     the input row
     * @param context the import context
     * @return the transformed row
     */
    default Map<String, Object> transformRow(Map<String, Object> row, ImportContext context) {
        return row;
    }

    /**
     * @return the order in which this transformer should run
     */
    default int getOrder() {
        return 100;
    }
}
