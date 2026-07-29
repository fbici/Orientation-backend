package com.orientation.orientationapp.dataplat_validation.strategy;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationContext {

    private ImportContext importContext;
    private DataType dataType;
    private Set<String> requiredColumns;
    private Set<String> optionalColumns;
    private Map<String, Set<String>> allowedValues;
    private Map<String, Object> referenceData;
    private UUID academicYearId;
    private UUID countryId;

    public static ValidationContext forImport(ImportContext importContext) {
        return ValidationContext.builder()
                .importContext(importContext)
                .dataType(importContext.getDataType())
                .academicYearId(importContext.getAcademicYearId())
                .countryId(importContext.getCountryId())
                .build();
    }
}
