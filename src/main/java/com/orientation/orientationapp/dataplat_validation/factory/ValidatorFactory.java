package com.orientation.orientationapp.dataplat_validation.factory;

import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_formats.enums.ValidationLevel;
import com.orientation.orientationapp.dataplat_validation.strategy.FieldValidator;

import java.util.List;

/**
 * Factory for creating validators based on data type and validation level.
 */
public interface ValidatorFactory {

    /**
     * Get all validators for a specific data type.
     *
     * @param dataType the data type
     * @return list of validators
     */
    List<FieldValidator> getValidators(DataType dataType);

    /**
     * Get validators for a specific data type and level.
     *
     * @param dataType the data type
     * @param level    the validation level
     * @return list of validators
     */
    List<FieldValidator> getValidators(DataType dataType, ValidationLevel level);

    /**
     * Get all available validators.
     *
     * @return list of all validators
     */
    List<FieldValidator> getAllValidators();
}
