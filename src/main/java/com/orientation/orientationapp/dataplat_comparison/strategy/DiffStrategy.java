package com.orientation.orientationapp.dataplat_comparison.strategy;

import com.orientation.orientationapp.dataplat_formats.enums.DataType;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for computing diffs between two versions of data.
 */
public interface DiffStrategy {

    /**
     * @return the data type this strategy handles
     */
    DataType getDataType();

    /**
     * Compute differences between two versions.
     *
     * @param oldData the data from the old version
     * @param newData the data from the new version
     * @return the diff result
     */
    DiffResult computeDiff(List<Map<String, Object>> oldData, List<Map<String, Object>> newData);

    /**
     * Compute differences using custom key extractors.
     *
     * @param oldData     the old version data
     * @param newData     the new version data
     * @param keyFunction function to extract unique keys
     * @return the diff result
     */
    DiffResult computeDiff(List<Map<String, Object>> oldData, List<Map<String, Object>> newData,
                           java.util.function.Function<Map<String, Object>, String> keyFunction);
}
