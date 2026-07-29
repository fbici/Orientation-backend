package com.orientation.orientationapp.dataplat_comparison.factory;

import com.orientation.orientationapp.dataplat_comparison.strategy.DiffStrategy;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;

/**
 * Factory for creating diff strategies.
 */
public interface DiffStrategyFactory {

    /**
     * Get the diff strategy for a specific data type.
     *
     * @param dataType the data type
     * @return the diff strategy
     */
    DiffStrategy getStrategy(DataType dataType);
}
