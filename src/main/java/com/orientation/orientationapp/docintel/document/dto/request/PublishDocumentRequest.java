package com.orientation.orientationapp.docintel.document.dto.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
import java.time.LocalDate;
import java.util.List;
public record PublishDocumentRequest(@NotNull UUID countryId,@NotNull UUID academicYearId,
 @NotBlank @Size(max=300) String guideTitle,@NotNull LocalDate effectiveDate,LocalDate expiryDate,
 @NotEmpty List<UUID> factIds,@Size(max=200) String publisher) {}
