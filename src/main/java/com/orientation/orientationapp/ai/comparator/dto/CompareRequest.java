package com.orientation.orientationapp.ai.comparator.dto;
import jakarta.validation.constraints.*;
import java.util.*;
public record CompareRequest(UUID candidateId,@NotNull @Size(min=2,max=6) List<@NotNull UUID> programIds) {}
