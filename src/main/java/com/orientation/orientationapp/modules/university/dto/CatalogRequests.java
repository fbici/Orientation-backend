package com.orientation.orientationapp.modules.university.dto;
import jakarta.validation.constraints.*;
import com.orientation.orientationapp.common.enums.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;
public final class CatalogRequests {
 private CatalogRequests() {}
 public record UniversityWrite(@NotBlank @Size(max=200) String name,@Size(max=50) String shortName,
  @NotNull UUID countryId,UUID cityId,@Size(max=30) String type,UniversityStatus status,
  @Size(max=500) String address,@Size(max=20) String phone,@Email @Size(max=255) String email,
  @Size(max=500) String website,@Min(1) Integer foundedYear,@Size(max=20000) String description,
  @Size(max=500) String sourceUrl) {}
 public record CampusWrite(@NotBlank @Size(max=200) String name,@NotNull UUID universityId,
  @NotNull UUID cityId,@Size(max=500) String address,@Size(max=20) String phone,Boolean main,Boolean active) {}
 public record FacultyWrite(@NotBlank @Size(max=200) String name,@NotNull UUID campusId,
  @Size(max=20) String code,@Size(max=20000) String description,Boolean active) {}
 public record ProgramWrite(@NotBlank @Size(max=200) String name,@NotNull UUID facultyId,
  @Size(max=30) String code,@NotNull ProgramType type,@Size(max=100) String degree,
  @NotNull @Min(1) @Max(30) Integer duration,@Size(max=50) String language,@Size(max=150) String studyField,
  @Size(max=20000) String description,@Size(max=20000) String prerequisites,
  @DecimalMin("0") BigDecimal tuitionFee,@Pattern(regexp="[A-Z]{3}") String tuitionCurrency,
  Boolean available,UUID academicYearId,@Size(max=500) String website,@Size(max=500) String sourceUrl) {}
 public record ScholarshipWrite(@NotBlank @Size(max=200) String name,@NotNull UUID countryId,
  @NotNull UUID academicYearId,@NotNull ScholarshipType type,@NotNull ScholarshipStatus status,
  @Size(max=200) String provider,@Size(max=20000) String description,@Size(max=500) String coverage,
  @DecimalMin("0") BigDecimal amount,@Pattern(regexp="[A-Z]{3}") String currency,@Min(1) Integer duration,
  LocalDate deadline,LocalDate validFrom,LocalDate validUntil,@Size(max=500) String applicationUrl,
  @Size(max=500) String sourceUrl,UUID universityId,UUID programId,Boolean active) {}
}
