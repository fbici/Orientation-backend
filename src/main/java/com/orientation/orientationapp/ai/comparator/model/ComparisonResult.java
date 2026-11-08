package com.orientation.orientationapp.ai.comparator.model;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import java.util.*;
/** Persisted comparison, not an official university ranking. */
public record ComparisonResult(UUID id,String kind,UUID candidateId,java.time.Instant computedAt,
    List<Map<String,Object>> programs,Map<String,List<Object>> differences,List<String> warnings) {}
