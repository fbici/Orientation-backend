package com.orientation.orientationapp.modules.transcript.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
public record TranscriptReview(
    @NotNull UUID gradeScaleId, @NotBlank @Size(max=100) String period,
    @Digits(integer=5,fraction=8) BigDecimal average,
    @NotNull @Size(max=200) List<@Valid Mark> marks
) {
    public record Mark(@NotBlank @Size(max=200) String rawSubject, UUID subjectId,
        @Size(max=30) String rawGrade, @Digits(integer=5,fraction=8) BigDecimal value,
        @DecimalMin(value="0",inclusive=false) @Digits(integer=3,fraction=4) BigDecimal coefficient,
        @Min(1) int page, @NotNull @Size(max=2000) String passage) {}
}
