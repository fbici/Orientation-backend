package com.orientation.orientationapp.modules.scholarship.dto;
import com.orientation.orientationapp.common.enums.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import java.math.BigDecimal;
public record ScholarshipRuleWrite(@NotNull UUID scholarshipId,@NotNull CriterionType criterionType,
 @NotNull CriterionOperator operator,UUID subjectId,BigDecimal minValue,BigDecimal maxValue,
 @Size(max=200) String stringValue,@NotNull Boolean mandatory,@Size(max=500) String description,
 @Size(max=50) String fieldName,@DecimalMin("0.01") BigDecimal valueScaleMax, @Size(max=50) String logicalGroup, LogicalOperator logicalOperator) {}
