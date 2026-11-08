package com.orientation.orientationapp.modules.orientation.dto;
import com.orientation.orientationapp.common.enums.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import java.math.BigDecimal;
public record AdmissionRuleWrite(@NotNull UUID programId,@NotNull CriterionType criterionType,@NotNull CriterionOperator operator,
 UUID subjectId,BigDecimal minValue,BigDecimal maxValue,@Size(max=200) String stringValue,
 @NotBlank @Size(max=50) String logicalGroup,@NotNull LogicalOperator logicalOperator,
 @NotNull Boolean mandatory,@Size(max=500) String description,@Size(max=50) String fieldName,
 @DecimalMin("0.01") BigDecimal valueScaleMax) {}
