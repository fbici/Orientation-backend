package com.orientation.orientationapp.modules.user.dto;

import com.orientation.orientationapp.common.enums.BacType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public record AcademicProfileRequest(
    @NotNull Long version,
    UUID countryId, UUID academicYearId, UUID gradesAcademicYearId, UUID gradeScaleId, UUID transcriptAnalysisId,
    @Size(max=100) String educationLevel, @Size(max=100) String diploma,
    @Size(max=100) String specialization, @Size(max=200) String highSchool,
    @Past LocalDate dateOfBirth, @Min(1900) @Max(2200) Integer bacYear, BacType bacType, @Size(max=30) String bacSeries,
    @Digits(integer=5,fraction=8) BigDecimal generalAverage,
    @Size(max=150) Map<@NotBlank @Size(max=20) String, @NotNull @Digits(integer=5,fraction=8) BigDecimal> subjectGrades,
    @Size(max=30) List<@NotBlank @Size(max=150) String> interests,
    @Size(max=30) List<@NotBlank @Size(max=100) String> preferredCountries,
    @Size(max=15) List<@NotBlank @Size(max=50) String> preferredLanguages,
    @Size(max=50) String language,
    @DecimalMin("0") @Digits(integer=10,fraction=2) BigDecimal budget,
    @Pattern(regexp="[A-Z]{3}") String budgetCurrency,
    UUID nationalityCountryId,
    @DecimalMin("0") @Digits(integer=10,fraction=2) BigDecimal householdIncome,
    @Pattern(regexp="[A-Z]{3}") String incomeCurrency,
    @Pattern(regexp="YEAR|MONTH") String incomePeriod,
    boolean confirmed
) {}
