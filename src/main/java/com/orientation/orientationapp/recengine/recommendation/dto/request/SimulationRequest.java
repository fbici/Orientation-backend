package com.orientation.orientationapp.recengine.recommendation.dto.request;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SimulationRequest {
 private UUID candidateId;
 private UUID academicYearId;
 @Size(max=50) private List<@NotNull UUID> programIds;
 @Size(max=150) private Map<@NotBlank @Size(max=20) String,@NotNull @Digits(integer=5,fraction=8) BigDecimal> modifiedGrades;
 @Size(max=30) private String modifiedBacType;
 @Size(max=30) private String modifiedBacSeries;
 @Digits(integer=5,fraction=8) private BigDecimal modifiedBacAverage;
 @Digits(integer=5,fraction=8) private BigDecimal modifiedGeneralAverage;
 @Size(max=100) private String modifiedCountry;
 @Size(max=30) private List<@NotBlank @Size(max=150) String> modifiedFields;
 @Size(max=30) private List<@NotBlank @Size(max=100) String> modifiedCountries;
 @Size(max=15) private List<@NotBlank @Size(max=50) String> modifiedLanguages;
 @DecimalMin("0") @Digits(integer=10,fraction=2) private BigDecimal modifiedBudget;
 @Size(max=50) private String modifiedLanguage;
 @Size(max=500) private String description;
}
