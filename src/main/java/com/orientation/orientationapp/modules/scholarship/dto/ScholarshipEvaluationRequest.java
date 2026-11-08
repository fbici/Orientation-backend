package com.orientation.orientationapp.modules.scholarship.dto;
import jakarta.validation.constraints.*;
import java.util.*;
public record ScholarshipEvaluationRequest(UUID candidateId,UUID programId,@Size(max=100) List<@NotNull UUID> scholarshipIds) {}
