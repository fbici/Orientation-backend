package com.orientation.orientationapp.modules.orientation.service;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.orientation.dto.AdmissionRuleWrite;
import com.orientation.orientationapp.modules.scholarship.dto.ScholarshipRuleWrite;
import com.orientation.orientationapp.modules.scholarship.entity.ScholarshipCriterion;
import com.orientation.orientationapp.modules.scholarship.repository.*;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import com.orientation.orientationapp.modules.university.service.*;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.row;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.found;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AdmissionRuleService {
 private final AdmissionCriterionRepository criteria;
 private final ScholarshipCriterionRepository scholarshipCriteria;
 private final SubjectRepository subjects;
 private final AcademicCatalogService catalogue;
 private final CatalogAccess scope;
 private final ResourceAccess access;
 private final com.orientation.orientationapp.docintel.document.repository.ExtractedFactRepository sourceFacts;
 private Map<String,Object> source(UUID id) {
  if(id==null)return null;
  return sourceFacts.findById(id).map(f->row("factId",f.getId(),"documentId",f.getDocument().getId(),"documentVersionId",f.getDocumentVersion().getId(),
   "sourceVersion",f.getDocumentVersion().getVersionNumber(),"checksum",f.getDocumentVersion().getChecksum(),"pageNumber",f.getPageNumber(),"passage",f.getSourcePassage(),"reviewedBy",f.getReviewedBy(),"reviewedAt",f.getReviewedAt())).orElse(null);
 }

 private void owner(UUID expected,UUID actual) {if(!Objects.equals(expected,actual))throw ResourceAccess.denied();}
 private Subject subject(UUID id,UUID country,UUID year) {
  if(id==null)return null;var subject=found(subjects.findById(id));
  if(!subject.getGradeScale().getCountry().getId().equals(country) || !subject.getGradeScale().getAcademicYear().getId().equals(year))
   throw invalid("Subject grading scale must match the programme country and guide year");
  return subject;
 }
 @Transactional public AdmissionCriterion publish(ExtractedFact fact,GuideVersion guide,AdmissionRuleWrite request) {
  access.require("RULE_PUBLISH");
  var program=catalogue.program(request.programId());var university=program.getFaculty().getCampus().getUniversity();
  owner(UUID.fromString(fact.getDocument().getTenantId()),university.getTenantId());
  if(!university.getCountry().getId().equals(guide.getOrientationGuide().getCountry().getId()))throw invalid("Rule programme country differs from the guide country");
  if(program.getAcademicYearId()!=null && !program.getAcademicYearId().equals(guide.getOrientationGuide().getAcademicYear().getId()))throw invalid("Rule programme academic year differs from the guide year");
  if(!Boolean.TRUE.equals(program.getVerified()))throw invalid("Validate the referenced programme before publishing its rules");
  var c=criteria.findBySourceFactId(fact.getId()).orElseGet(AdmissionCriterion::new);
  c.setGuideVersion(guide);c.setProgram(program);c.setCriterionType(request.criterionType());c.setOperator(request.operator());
  c.setSubject(subject(request.subjectId(),university.getCountry().getId(),guide.getOrientationGuide().getAcademicYear().getId()));
  c.setMinValue(request.minValue());c.setMaxValue(request.maxValue());c.setStringValue(request.stringValue());c.setMandatory(request.mandatory());
  c.setLogicalGroup(request.logicalGroup());c.setLogicalOperator(request.logicalOperator());c.setDescription(request.description());
  c.setFieldName(request.fieldName());c.setValueScaleMax(request.valueScaleMax());c.setSourceFactId(fact.getId());
  return criteria.saveAndFlush(c);
 }
 @Transactional public ScholarshipCriterion publishScholarship(ExtractedFact fact,GuideVersion guide,ScholarshipRuleWrite request) {
  access.require("RULE_PUBLISH");access.require("SCHOLARSHIP_MANAGE");
  var scholarship=catalogue.scholarship(request.scholarshipId());owner(UUID.fromString(fact.getDocument().getTenantId()),scholarship.getTenantId());
  if(!Boolean.TRUE.equals(scholarship.getVerified()))throw invalid("Validate the referenced scholarship first");
  if(!scholarship.getCountry().getId().equals(guide.getOrientationGuide().getCountry().getId()) || !scholarship.getAcademicYear().getId().equals(guide.getOrientationGuide().getAcademicYear().getId()))throw invalid("Scholarship criterion scope differs from its guide country or year");
  var c=scholarshipCriteria.findBySourceFactId(fact.getId()).orElseGet(ScholarshipCriterion::new);
  c.setGuideVersion(guide);c.setScholarship(scholarship);c.setCriterionType(request.criterionType());c.setOperator(request.operator());
  c.setSubject(subject(request.subjectId(),scholarship.getCountry().getId(),guide.getOrientationGuide().getAcademicYear().getId()));
  c.setMinValue(request.minValue());c.setMaxValue(request.maxValue());c.setStringValue(request.stringValue());c.setMandatory(request.mandatory());
  c.setLogicalGroup(request.logicalGroup()==null?"DEFAULT":request.logicalGroup());
  c.setLogicalOperator(request.logicalOperator()==null?com.orientation.orientationapp.common.enums.LogicalOperator.AND:request.logicalOperator());
  c.setDescription(request.description());c.setFieldName(request.fieldName());c.setValueScaleMax(request.valueScaleMax());c.setSourceFactId(fact.getId());
  return scholarshipCriteria.saveAndFlush(c);
 }
 public Map<String,Object> view(AdmissionCriterion c) {
  return row("id",c.getId(),"programId",c.getProgram()==null?null:c.getProgram().getId(),"programName",c.getProgram()==null?null:c.getProgram().getName(),
   "guideVersionId",c.getGuideVersion().getId(),"guideVersionNumber",c.getGuideVersion().getVersionNumber(),
   "academicYearId",c.getGuideVersion().getOrientationGuide().getAcademicYear().getId(),
   "criterionType",c.getCriterionType(),"operator",c.getOperator(),"subjectId",c.getSubject()==null?null:c.getSubject().getId(),
   "minValue",c.getMinValue(),"maxValue",c.getMaxValue(),"stringValue",c.getStringValue(),"mandatory",c.getMandatory(),
   "logicalGroup",c.getLogicalGroup(),"logicalOperator",c.getLogicalOperator(),"description",c.getDescription(),
   "fieldName",c.getFieldName(),"valueScaleMax",c.getValueScaleMax(),"sourceFactId",c.getSourceFactId(),"source",source(c.getSourceFactId()),"verificationStatus","VALIDATED");
 }
 public Page<Map<String,Object>> list(UUID programId,UUID guideVersionId,int page,int size) {
  if(page<0 || size<1 || size>100)throw invalid("Invalid rule pagination");
  UUID tenant=scope.tenant();boolean global=scope.global();
  return criteria.findAll((r,q,cb)-> {
   var tests=new ArrayList<jakarta.persistence.criteria.Predicate>();var guide=r.get("guideVersion");
   tests.add(cb.isNotNull(r.get("sourceFactId")));
   if(programId!=null)tests.add(cb.equal(r.get("program").get("id"),programId));
   if(guideVersionId!=null)tests.add(cb.equal(guide.get("id"),guideVersionId));
   else tests.add(cb.equal(guide.get("status"),com.orientation.orientationapp.common.enums.GuideVersionStatus.ACTIVE));
   var owner=guide.get("orientationGuide").get("tenantId");
   if(!global)tests.add(tenant==null?cb.isNull(owner):cb.or(cb.isNull(owner),cb.equal(owner,tenant)));
   return cb.and(tests.toArray(jakarta.persistence.criteria.Predicate[]::new));
  },PageRequest.of(page,size,Sort.by("priority").ascending())).map(this::view);
 }
 public Map<String,Object> get(UUID id) {
  var c=found(criteria.findById(id));scope.read(c.getGuideVersion().getOrientationGuide().getTenantId());
  if(c.getSourceFactId()==null)throw invalid("This legacy rule has no validated source");return view(c);
 }
 public List<Map<String,Object>> scholarshipRules(UUID scholarshipId) {
  catalogue.get("scholarships",scholarshipId);
  return scholarshipCriteria.findByScholarshipId(scholarshipId).stream().filter(c->c.getSourceFactId()!=null).map(c->row(
   "id",c.getId(),"scholarshipId",scholarshipId,"guideVersionId",c.getGuideVersion().getId(),"criterionType",c.getCriterionType(),
   "operator",c.getOperator(),"subjectId",c.getSubject()==null?null:c.getSubject().getId(),"minValue",c.getMinValue(),"maxValue",c.getMaxValue(),
   "stringValue",c.getStringValue(),"mandatory",c.getMandatory(),"logicalGroup",c.getLogicalGroup(),"logicalOperator",c.getLogicalOperator(),"description",c.getDescription(),"fieldName",c.getFieldName(),
   "valueScaleMax",c.getValueScaleMax(),"sourceFactId",c.getSourceFactId(),"source",source(c.getSourceFactId()),"verificationStatus","VALIDATED")).toList();
 }
}
