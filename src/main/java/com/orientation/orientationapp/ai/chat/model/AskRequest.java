package com.orientation.orientationapp.ai.chat.model;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.orientation.orientationapp.recengine.recommendation.dto.request.SimulationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
public record AskRequest(@JsonAlias("content") @NotBlank @Size(max=2000) String message,
 UUID recommendationId,@Size(max=6) List<@NotNull UUID> programIds,@Valid SimulationRequest simulation) {}
