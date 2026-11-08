package com.orientation.orientationapp.recengine;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import com.orientation.orientationapp.recengine.simulation.engine.impl.DefaultSimulationEngine;
import com.orientation.orientationapp.recengine.recommendation.dto.request.SimulationRequest;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.university.entity.Country;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SimulationEngineTest {
 final AcademicYearRepository years=mock(AcademicYearRepository.class);
 final SubjectRepository subjects=mock(SubjectRepository.class);
 final DefaultSimulationEngine engine=new DefaultSimulationEngine(null,null,new GradeNormalizer(),years,subjects,mock(CountryRepository.class));
 GradeScale scale(){return GradeScale.builder().country(new Country()).academicYear(new AcademicYear()).minScore(BigDecimal.ZERO).maxScore(new BigDecimal("20")).normalizeTo(new BigDecimal("20")).build();}
 @Test void simulationUsesAnIndependentCopyOfGradesAndPreferences() {
  var original=AcademicProfile.builder().subjectGrades(new LinkedHashMap<>(Map.of("MATH",new BigDecimal("12")))).preferredFields(new ArrayList<>(List.of("SCIENCE"))).build();
  var copy=engine.copy(original);copy.getSubjectGrades().put("MATH",new BigDecimal("19"));copy.getPreferredFields().clear();
  assertEquals(new BigDecimal("12"),original.getSubjectGrades().get("MATH"));assertEquals(List.of("SCIENCE"),original.getPreferredFields());
 }
 @Test void appliesAnExplicitAverageWithoutTouchingTheReference() {
  var original=AcademicProfile.builder().generalAverage(new BigDecimal("11")).gradeScale(scale()).build();
  var copy=engine.copy(original);var request=new SimulationRequest();request.setModifiedGeneralAverage(new BigDecimal("13"));engine.apply(copy,request);
  assertEquals(new BigDecimal("11"),original.getGeneralAverage());assertEquals(new BigDecimal("13"),copy.getGeneralAverage());
 }
 @Test void rejectsGradesOutsideTheExplicitScale() {var r=new SimulationRequest();r.setModifiedGeneralAverage(new BigDecimal("21"));assertThrows(org.springframework.web.server.ResponseStatusException.class,()->engine.apply(AcademicProfile.builder().gradeScale(scale()).build(),r));}
 @Test void rejectsAnUnspecifiedScale() {var r=new SimulationRequest();r.setModifiedGeneralAverage(BigDecimal.TEN);assertThrows(org.springframework.web.server.ResponseStatusException.class,()->engine.apply(AcademicProfile.builder().build(),r));}
 @Test void rejectsUnknownYearsRatherThanGuessingRules() {var r=new SimulationRequest();r.setAcademicYearId(UUID.randomUUID());assertThrows(org.springframework.web.server.ResponseStatusException.class,()->engine.apply(AcademicProfile.builder().build(),r));}
 @Test void rejectsUnknownSubjectCodes() {var s=scale();s.setId(UUID.randomUUID());when(subjects.findByGradeScaleId(s.getId())).thenReturn(List.of());var r=new SimulationRequest();r.setModifiedGrades(Map.of("UNKNOWN",BigDecimal.TEN));assertThrows(org.springframework.web.server.ResponseStatusException.class,()->engine.apply(AcademicProfile.builder().gradeScale(s).build(),r));}
 @Test void rejectsContradictoryAverageAliases() {var r=new SimulationRequest();r.setModifiedGeneralAverage(BigDecimal.ONE);r.setModifiedBacAverage(BigDecimal.TEN);assertThrows(org.springframework.web.server.ResponseStatusException.class,()->engine.apply(AcademicProfile.builder().build(),r));}
 @Test void reportsRealEligibilityTransitionsAndExactScoreDeltas() {
  var id=UUID.randomUUID();var a=Map.<String,Object>of("programId",id,"eligibilityStatus","NOT_ELIGIBLE","score",new BigDecimal("41.25"));
  var b=Map.<String,Object>of("programId",id,"eligibilityStatus","ELIGIBLE","score",new BigDecimal("47.75"));
  var diff=DefaultSimulationEngine.differences(List.of(a),List.of(b)).getFirst();
  assertEquals(true,diff.get("becameEligible"));assertEquals(new BigDecimal("6.50"),diff.get("scoreDelta"));
 }
 @Test void unknownScoreAndEligibilityRemainUnknown() {
  var row=new LinkedHashMap<String,Object>();row.put("programId",UUID.randomUUID());row.put("eligibilityStatus","INSUFFICIENT_DATA");row.put("score",null);
  var diff=DefaultSimulationEngine.differences(List.of(row),List.of(row)).getFirst();
  assertNull(diff.get("scoreDelta"));assertEquals(true,diff.get("stillInsufficient"));assertEquals(false,diff.get("becameEligible"));
 }
}
