package com.orientation.orientationapp.docintel.document.service;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository;
import com.orientation.orientationapp.modules.university.dto.CatalogRequests.*;
import com.orientation.orientationapp.modules.orientation.dto.AdmissionRuleWrite;
import com.orientation.orientationapp.modules.scholarship.dto.ScholarshipRuleWrite;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid;
import java.util.*;
import java.math.BigDecimal;
@Component @RequiredArgsConstructor
public class FactPayloadMapper {
 private final ObjectMapper json;
 private final Validator validator;
 private final ExtractedFactRepository facts;
 private final com.orientation.orientationapp.modules.university.service.AcademicCatalogService catalogue;
 private final com.orientation.orientationapp.modules.university.repository.CountryRepository countries;
 private final com.orientation.orientationapp.modules.university.repository.CityRepository cities;
 private final com.orientation.orientationapp.modules.orientation.repository.AcademicYearRepository years;
 private final com.orientation.orientationapp.modules.orientation.repository.SubjectRepository subjects;

 public static final Set<String> KINDS=Set.of("UNIVERSITY","CAMPUS","FACULTY","PROGRAM","SCHOLARSHIP","ADMISSION_CRITERION","SCHOLARSHIP_CRITERION",
  "COUNTRY","ACADEMIC_YEAR","BAC_REQUIREMENT","DIPLOMA_REQUIREMENT","SUBJECT_REQUIREMENT","DOCUMENT_REQUIREMENT","FEE","DATE","ACADEMIC_ATTRIBUTE");
 public Map<String,Object> values(ExtractedFact fact) {
  try {return json.readValue(fact.getPayload(),new TypeReference<LinkedHashMap<String,Object>>(){});}
  catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException("Stored draft is invalid");}
 }
 public String encodeFact(Object values) {String text=encode(values);if(text.length()>100000)throw invalid("Fact payload exceeds its limit");return text;}
 public String encode(Object values) {
  try {String encoded=json.writeValueAsString(values);if(encoded.length()>2000000)throw invalid("Fact payload exceeds its limit");return encoded;}
  catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw invalid("Fact values cannot be serialized");}
 }
 public Class<?> type(String kind) {
  return switch(kind) {case "UNIVERSITY" -> UniversityWrite.class;case "CAMPUS" -> CampusWrite.class;case "FACULTY" -> FacultyWrite.class;
   case "PROGRAM" -> ProgramWrite.class;case "SCHOLARSHIP" -> ScholarshipWrite.class;case "ADMISSION_CRITERION" -> AdmissionRuleWrite.class;
   case "SCHOLARSHIP_CRITERION" -> ScholarshipRuleWrite.class;default -> null;};
 }
 public UUID uuid(Object value) {
  try {return UUID.fromString(String.valueOf(value));}catch(IllegalArgumentException ex){throw invalid("Invalid academic reference identifier");}
 }
 public Map<String,Object> resolve(ExtractedFact source,Map<String,Object> values,boolean publishing) {
  var result=new LinkedHashMap<>(values);
  for(String parent:List.of("university","campus","faculty","program","scholarship")) {
   String factKey=parent+"FactId",entityKey=parent+"Id";
   if(values.get(factKey)==null)continue;
   if(values.get(entityKey)!=null)throw invalid("Choose an existing "+parent+" or a draft reference, not both");
   var target=facts.findById(uuid(values.get(factKey))).orElseThrow(()->invalid("Referenced draft is unavailable"));
   if(!target.getDocumentVersion().getId().equals(source.getDocumentVersion().getId()) || !target.getKind().equals(parent.toUpperCase(Locale.ROOT)))
    throw invalid("Referenced draft must belong to this source version and entity kind");
   if(publishing) {
    if(!"VALIDATED".equals(target.getStatus()) || target.getPublishedEntityId()==null)
     throw invalid("Referenced draft must be validated and published first");
    result.remove(factKey);result.put(entityKey,target.getPublishedEntityId());
   }
  }
  return result;
 }
 public Object request(ExtractedFact fact,Map<String,Object> values,boolean publishing) {
  if(!KINDS.contains(fact.getKind()))throw invalid("Unsupported fact kind");
  Class<?> type=type(fact.getKind());
  if(type==null) {
   if(values.isEmpty())throw invalid("Verified academic information cannot be empty");
   return values;
  }
  Map<String,Object> resolved=resolve(fact,values,publishing);
  Object request;
  try {
   // Record components are the only business write fields. Source annotations remain in the fact JSON.
   var permitted=new LinkedHashMap<String,Object>();
   for(var component:type.getRecordComponents())if(resolved.containsKey(component.getName()))permitted.put(component.getName(),resolved.get(component.getName()));
   request=json.convertValue(permitted,type);
  } catch(IllegalArgumentException ex){throw invalid("Invalid field type in academic information");}
  var invalidFields=new TreeSet<String>();
  for(var violation:validator.validate(request)) {
   String field=violation.getPropertyPath().toString();
   boolean deferred=!publishing && Set.of("universityId","campusId","facultyId","programId","scholarshipId").contains(field) && values.get(field.substring(0,field.length()-2)+"FactId")!=null;
   if(!deferred)invalidFields.add(field);
  }
  if(!invalidFields.isEmpty())throw invalid("Required or invalid fields: "+String.join(", ",invalidFields));
  references(request);
  if(request instanceof AdmissionRuleWrite r) rule(r.criterionType(),r.operator(),r.subjectId(),r.minValue(),r.maxValue(),r.stringValue(),r.fieldName(),r.valueScaleMax());
  if(request instanceof ScholarshipRuleWrite r)rule(r.criterionType(),r.operator(),r.subjectId(),r.minValue(),r.maxValue(),r.stringValue(),r.fieldName(),r.valueScaleMax());
  return request;
 }
 private void references(Object request) {
  if(request instanceof UniversityWrite r) {
   if(countries.findById(r.countryId()).isEmpty())throw invalid("Country reference is unavailable");
   var c=cities.findById(r.cityId()).orElseThrow(()->invalid("City reference is unavailable"));
   if(!c.getCountry().getId().equals(r.countryId()))throw invalid("City and country differ");
  } else if(request instanceof CampusWrite r) {
   var c=cities.findById(r.cityId()).orElseThrow(()->invalid("City reference is unavailable"));
   if(r.universityId()!=null && !catalogue.university(r.universityId()).getCountry().getId().equals(c.getCountry().getId()))throw invalid("Campus city and university country differ");
  } else if(request instanceof FacultyWrite r) {
   if(r.campusId()!=null)catalogue.campus(r.campusId());
  } else if(request instanceof ProgramWrite r) {
   if(r.facultyId()!=null)catalogue.faculty(r.facultyId());
   if(r.academicYearId()!=null && years.findById(r.academicYearId()).isEmpty())throw invalid("Academic year is unavailable");
   if(r.tuitionFee()!=null && r.tuitionCurrency()==null)throw invalid("Tuition currency is required");
  } else if(request instanceof ScholarshipWrite r) {
   if(countries.findById(r.countryId()).isEmpty() || years.findById(r.academicYearId()).isEmpty())throw invalid("Scholarship country or academic year is unavailable");
   if(r.universityId()!=null)catalogue.university(r.universityId());if(r.programId()!=null)catalogue.program(r.programId());
   if(r.amount()!=null && r.currency()==null)throw invalid("Scholarship currency is required");
   if(r.validFrom()!=null && r.validUntil()!=null && r.validUntil().isBefore(r.validFrom()))throw invalid("Scholarship validity dates are reversed");
  } else if(request instanceof AdmissionRuleWrite r) {
   if(r.programId()!=null)catalogue.program(r.programId());if(r.subjectId()!=null && subjects.findById(r.subjectId()).isEmpty())throw invalid("Subject is unavailable");
  } else if(request instanceof ScholarshipRuleWrite r) {
   if(r.scholarshipId()!=null)catalogue.scholarship(r.scholarshipId());if(r.subjectId()!=null && subjects.findById(r.subjectId()).isEmpty())throw invalid("Subject is unavailable");
  }
  for(var component:request.getClass().getRecordComponents())if(component.getName().endsWith("Url") || component.getName().equals("website")) {
   try {
    Object raw=component.getAccessor().invoke(request);if(raw==null || raw.toString().isBlank())continue;
    var uri=java.net.URI.create(raw.toString());if(!Set.of("http","https").contains(uri.getScheme()) || uri.getHost()==null)throw invalid("Academic URLs must use HTTP(S)");
   } catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    catch(IllegalArgumentException ex){throw invalid("Invalid academic URL");}
  }
 }
 private void rule(com.orientation.orientationapp.common.enums.CriterionType type,com.orientation.orientationapp.common.enums.CriterionOperator op,
  UUID subject,BigDecimal min,BigDecimal max,String value,String field,BigDecimal scale) {
  if(Set.of(com.orientation.orientationapp.common.enums.CriterionType.SPECIFIC_GRADE,com.orientation.orientationapp.common.enums.CriterionType.MIN_GRADE,com.orientation.orientationapp.common.enums.CriterionType.MAX_GRADE).contains(type) && subject==null)
   throw invalid("A subject is required for a subject-grade rule");
  switch(op) {
   case GREATER_THAN,GREATER_THAN_OR_EQUAL -> {if(min==null)throw invalid("A lower value is required");}
   case LESS_THAN,LESS_THAN_OR_EQUAL -> {if(max==null)throw invalid("An upper value is required");}
   case BETWEEN -> {if(min==null || max==null || min.compareTo(max)>0)throw invalid("A valid interval is required");}
   case IN_LIST,NOT_IN_LIST -> {if(value==null || value.isBlank())throw invalid("Explicit accepted/rejected values are required");}
   case EQUAL,NOT_EQUAL -> {if(min==null && (value==null || value.isBlank()))throw invalid("An explicit comparison value is required");}
  }
  if(type==com.orientation.orientationapp.common.enums.CriterionType.CUSTOM) {
   if(field==null || !Set.of("bacType","bacSeries","diploma","educationLevel","requiredDiploma","language","nationality","nationalityCountryId","country","countryId","programId","universityId","studyField","householdIncome","residence","requiredDocument").contains(field))
    throw invalid("A supported academic field is required for a custom rule");
   if(value==null || value.isBlank())throw invalid("A custom requirement needs explicit source values");
  }
  if("householdIncome".equals(field) && (value==null || !value.matches("[A-Z]{3}/(MONTH|YEAR)")))throw invalid("Explicit income currency and period are required");
  if(scale!=null && (min!=null && min.compareTo(scale)>0 || max!=null && max.compareTo(scale)>0))
   throw invalid("Rule value exceeds its explicit scale");
 }
}
