package com.orientation.orientationapp.dataplat_transformation.factory;

import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_transformation.strategy.DataTransformer;

import java.util.List;

/**
 * Factory for creating data transformers.
 */
public interface TransformerFactory {

    /**
     * Get all transformers for a specific data type.
     *
     * @param dataType the data type
     * @return ordered list of transformers
     */
    List<DataTransformer> getTransformers(DataType dataType);

    /**
     * Get a specific transformer by ID.
     *
     * @param transformerId the transformer ID
     * @return the transformer
     */
    DataTransformer getTransformer(String transformerId);
}
