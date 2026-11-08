package com.orientation.orientationapp.intelligence.pipeline;
import com.orientation.orientationapp.docintel.document.entity.*;
import com.orientation.orientationapp.docintel.document.repository.*;
import com.orientation.orientationapp.docintel.document.dto.request.PublishDocumentRequest;
import com.orientation.orientationapp.docintel.document.service.FactPayloadMapper;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.orientation.dto.AdmissionRuleWrite;
import com.orientation.orientationapp.modules.orientation.service.AdmissionRuleService;
import com.orientation.orientationapp.modules.university.repository.*;
import com.orientation.orientationapp.modules.university.service.*;
import com.orientation.orientationapp.modules.university.dto.CatalogRequests.*;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.modules.scholarship.dto.ScholarshipRuleWrite;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.common.model.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.time.Instant;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.row;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.invalid;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.found;

/** Publishes the reviewed facts through the existing business repositories, graph and index, in one transaction. */
@Service @RequiredArgsConstructor
public class AcademicPublicationService {
 private final DocumentRepository documents;
 private final DocumentVersionRepository sourceVersions;
 private final ExtractedFactRepository facts;
 private final DocumentAuditRepository audits;
 private final OrientationGuideRepository guides;
 private final GuideVersionRepository guideVersions;
 private final CountryRepository countries;
 private final AcademicYearRepository years;
 private final UniversityRepository universities;
 private final CampusRepository campuses;
 private final FacultyRepository faculties;
 private final ProgramRepository programs;
 private final ScholarshipRepository scholarships;
 private final AcademicCatalogService catalogue;
 private final AdmissionRuleService rules;
 private final FactPayloadMapper payloads;
 private final ResourceAccess access;
 private final KnowledgeGraphService knowledge;

 @Transactional
 public Map<String,Object> publish(UUID documentId,PublishDocumentRequest request) {
  access.require("GUIDE_PUBLISH");
  var document=documents.lock(documentId).orElseThrow(ResourceAccess::denied);
  access.document(document.getTenantId(),document.getUploadedBy(),true);
  UUID owner=UUID.fromString(document.getTenantId());
  if("PROCESSING".equals(document.getStatus()) || "FAILED".equals(document.getStatus()) || "UPLOADED".equals(document.getStatus()))
   throw invalid("Extract and validate the source document before publication");
  if(request.factIds().size()>500 || new HashSet<>(request.factIds()).size()!=request.factIds().size())
   throw invalid("Choose at most 500 distinct source facts per publication");
  var selected=new ArrayList<ExtractedFact>();
  for(UUID id:new TreeSet<>(request.factIds())) {
   var f=facts.lock(id).orElseThrow(ResourceAccess::denied);
   if(!f.getDocument().getId().equals(documentId))throw ResourceAccess.denied();
   if(!"VALIDATED".equals(f.getStatus()) || f.getReviewedBy()==null)throw invalid("Every selected fact must be human validated");
   selected.add(f);
  }
  var sources=sourceVersions.findByDocumentIdOrderByVersionNumberDesc(documentId);
  if(sources.isEmpty())throw invalid("Stored source version unavailable");
  var source=sources.getFirst();
  if(selected.stream().anyMatch(f->!f.getDocumentVersion().getId().equals(source.getId())))throw invalid("Selected facts must use the current stored source version");
  if(request.expiryDate()!=null && request.expiryDate().isBefore(request.effectiveDate()))throw invalid("Guide validity dates are reversed");
  var country=found(countries.lock(request.countryId())); // Serialize a country's guide-version allocation across documents.
  var year=found(years.findById(request.academicYearId()));
  var guide=guides.findByCountryIdAndAcademicYearIdAndTenantId(country.getId(),year.getId(),owner).orElseGet(OrientationGuide::new);
  if(guide.getId()==null) {
   access.require("GUIDE_CREATE");
   guide.setCountry(country);guide.setAcademicYear(year);guide.setTenantId(owner);guide.setTitle(request.guideTitle());
   guide.setPublisher(request.publisher());guide.setActive(true);guides.saveAndFlush(guide);
  }
  var existing=guideVersions.findByDocumentVersionId(source.getId());
  GuideVersion version;
  if(existing.isPresent()) {
   version=existing.get();
   if(!version.getOrientationGuide().getId().equals(guide.getId()) || !version.getEffectiveDate().equals(request.effectiveDate())
    || !Objects.equals(version.getExpiryDate(),request.expiryDate()))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"Published source metadata is immutable; use a new source version");
   if(version.getStatus()!=GuideVersionStatus.ACTIVE && selected.stream().anyMatch(f->f.getPublishedAt()==null))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"Historical guide versions cannot accept new publications");
  } else {
   var previous=guideVersions.findByOrientationGuideId(guide.getId());
   int number=previous.stream().mapToInt(GuideVersion::getVersionNumber).max().orElse(0)+1;
   for(var old:previous)if(old.getStatus()==GuideVersionStatus.ACTIVE){old.setStatus(GuideVersionStatus.ARCHIVED);old.setActive(false);guideVersions.save(old);}
   version=new GuideVersion();version.setOrientationGuide(guide);version.setVersionNumber(number);version.setVersionLabel("v"+number);
   version.setDocumentVersionId(source.getId());version.setEffectiveDate(request.effectiveDate());version.setExpiryDate(request.expiryDate());
   version.setStatus(GuideVersionStatus.ACTIVE);version.setActive(true);guideVersions.saveAndFlush(version);
  }
  selected.sort(Comparator.comparingInt(f -> order(f.getKind())));
  var results=new ArrayList<Map<String,Object>>();int created=0;
  for(var fact:selected) {
   if(fact.getPublishedAt()!=null) {
    if(!version.getId().equals(fact.getGuideVersionId()))throw invalid("Fact was published in a different guide version");
    results.add(row("factId",fact.getId(),"type",fact.getPublishedEntityType(),"entityId",fact.getPublishedEntityId(),"reused",true));continue;
   }
   var values=payloads.resolve(fact,payloads.values(fact),true);
   if(fact.getKind().equals("PROGRAM") || fact.getKind().equals("SCHOLARSHIP")) {
    if(values.get("academicYearId")==null)values.put("academicYearId",year.getId());
    else if(!payloads.uuid(values.get("academicYearId")).equals(year.getId()))throw invalid("Entity academic year differs from the guide year");
   }
   Object bean=payloads.request(fact,values,true);
   UUID id=null;Map<String,Object> attributes;
   String entityType=fact.getKind();
   if(bean instanceof UniversityWrite r) {
    if(!r.countryId().equals(country.getId()))throw invalid("University country differs from guide country");
    id=target(values,()->naturalUniversity(r,owner));if(id!=null)sameOwner(owner,catalogue.university(id).getTenantId());
    var reviewed=(UniversityWrite)merged(fact,id,"universities",values);
    attributes=catalogue.saveUniversityOwned(id,reviewed,owner);id=payloads.uuid(attributes.get("id"));
    var u=catalogue.university(id);u.setVerified(true);universities.saveAndFlush(u);attributes=catalogue.view(u);
   } else if(bean instanceof CampusWrite r) {
    sameOwner(owner,catalogue.university(r.universityId()).getTenantId());
    id=target(values,()->unique(campuses.findAll((x,q,cb)->cb.and(cb.equal(cb.lower(x.get("name")),r.name().strip().toLowerCase(Locale.ROOT)),cb.equal(x.get("university").get("id"),r.universityId())),PageRequest.of(0,2)).getContent()));
    if(id!=null)sameOwner(owner,catalogue.campus(id).getUniversity().getTenantId());
    if(!Boolean.TRUE.equals(catalogue.university(r.universityId()).getVerified()))throw invalid("Publish the parent university first");
    if(!catalogue.university(r.universityId()).getCountry().getId().equals(country.getId()))throw invalid("Campus country differs from guide country");
    var reviewed=(CampusWrite)merged(fact,id,"campuses",values);attributes=catalogue.saveCampus(id,reviewed);id=payloads.uuid(attributes.get("id"));
    var c=catalogue.campus(id);c.setVerified(true);campuses.saveAndFlush(c);attributes=catalogue.view(c);
   } else if(bean instanceof FacultyWrite r) {
    sameOwner(owner,catalogue.campus(r.campusId()).getUniversity().getTenantId());
    id=target(values,()->unique(faculties.findAll((x,q,cb)->cb.and(cb.equal(cb.lower(x.get("name")),r.name().strip().toLowerCase(Locale.ROOT)),cb.equal(x.get("campus").get("id"),r.campusId())),PageRequest.of(0,2)).getContent()));
    if(id!=null)sameOwner(owner,catalogue.faculty(id).getCampus().getUniversity().getTenantId());
    if(!Boolean.TRUE.equals(catalogue.campus(r.campusId()).getVerified()))throw invalid("Publish the parent campus first");
    var reviewed=(FacultyWrite)merged(fact,id,"faculties",values);attributes=catalogue.saveFaculty(id,reviewed);id=payloads.uuid(attributes.get("id"));
    var f=catalogue.faculty(id);f.setVerified(true);faculties.saveAndFlush(f);attributes=catalogue.view(f);
   } else if(bean instanceof ProgramWrite r) {
    sameOwner(owner,catalogue.faculty(r.facultyId()).getCampus().getUniversity().getTenantId());
    id=target(values,()->unique(programs.findAll((x,q,cb)->cb.and(cb.equal(cb.lower(x.get("name")),r.name().strip().toLowerCase(Locale.ROOT)),cb.equal(x.get("faculty").get("id"),r.facultyId())),PageRequest.of(0,2)).getContent()));
    if(id!=null)sameOwner(owner,catalogue.program(id).getFaculty().getCampus().getUniversity().getTenantId());
    if(!Boolean.TRUE.equals(catalogue.faculty(r.facultyId()).getVerified()))throw invalid("Publish the parent faculty first");
    if(!catalogue.faculty(r.facultyId()).getCampus().getUniversity().getCountry().getId().equals(country.getId()))throw invalid("Programme country differs from guide country");
    var reviewed=(ProgramWrite)merged(fact,id,"programs",values);attributes=catalogue.saveProgram(id,reviewed);id=payloads.uuid(attributes.get("id"));
    var p=catalogue.program(id);p.setVerified(true);programs.saveAndFlush(p);attributes=catalogue.view(p);
   } else if(bean instanceof ScholarshipWrite r) {
    if(!r.countryId().equals(country.getId()))throw invalid("Scholarship country differs from guide country");
    id=target(values,()->unique(scholarships.findAll((x,q,cb)->cb.and(cb.equal(cb.lower(x.get("name")),r.name().strip().toLowerCase(Locale.ROOT)),cb.equal(x.get("country").get("id"),r.countryId()),cb.equal(x.get("academicYear").get("id"),r.academicYearId()),cb.equal(x.get("tenantId"),owner)),PageRequest.of(0,2)).getContent()));
    if(id!=null)sameOwner(owner,catalogue.scholarship(id).getTenantId());
    if(r.programId()!=null)sameOwner(owner,catalogue.program(r.programId()).getFaculty().getCampus().getUniversity().getTenantId());
    if(r.universityId()!=null)sameOwner(owner,catalogue.university(r.universityId()).getTenantId());
    var reviewed=(ScholarshipWrite)merged(fact,id,"scholarships",values);attributes=catalogue.saveScholarshipOwned(id,reviewed,owner);id=payloads.uuid(attributes.get("id"));
    var s=catalogue.scholarship(id);s.setVerified(true);scholarships.saveAndFlush(s);attributes=catalogue.view(s);
   } else if(bean instanceof AdmissionRuleWrite r) {
    var c=rules.publish(fact,version,r);id=c.getId();attributes=rules.view(c);
   } else if(bean instanceof ScholarshipRuleWrite r) {
    var c=rules.publishScholarship(fact,version,r);id=c.getId();
    attributes=row("id",id,"name",r.description()==null?"Scholarship requirement":r.description(),"scholarshipId",r.scholarshipId(),"criterionType",r.criterionType(),"operator",r.operator(),"minValue",r.minValue(),"maxValue",r.maxValue(),"stringValue",r.stringValue());
   } else {
    entityType="ACADEMIC_FACT";id=fact.getId();attributes=new LinkedHashMap<>(values);
   }
   fact.setPublishedEntityId(id);fact.setPublishedEntityType(entityType);fact.setGuideVersionId(version.getId());
   fact.setAcademicYearId(year.getId());fact.setPublishedAt(Instant.now());facts.saveAndFlush(fact);
   Object name=attributes.get("name");
   if(name==null)name=attributes.get("description");
   if(name==null)name=fact.getSourcePassage();
   knowledge.publish(fact,version,entityType,id,String.valueOf(name),attributes);
   results.add(row("factId",fact.getId(),"type",entityType,"entityId",id,"reused",false));created++;
  }
  var remaining=facts.findByDocumentIdOrderByPageNumberAscCreatedAtAsc(documentId);
  document.setStatus(remaining.stream().anyMatch(f->!"REJECTED".equals(f.getStatus()) && f.getPublishedAt()==null)?"PARTIALLY_PUBLISHED":"PUBLISHED");
  documents.saveAndFlush(document);
  audits.save(DocumentAudit.builder().document(document).action("PUBLISH_VALIDATED_FACTS").userId(access.current().getId().toString()).performedAt(Instant.now())
   .details(payloads.encode(row("guideVersionId",version.getId(),"results",results))).build());
  return row("documentId",documentId,"guideId",guide.getId(),"guideVersionId",version.getId(),"guideVersionNumber",version.getVersionNumber(),
   "publishedCount",created,"reusedCount",results.size()-created,"results",results,"status",document.getStatus(),"searchMode","TEXT");
 }
 private UUID naturalUniversity(UniversityWrite r,UUID owner) {
  return unique(universities.findAll((x,q,cb)->cb.and(cb.equal(cb.lower(x.get("name")),r.name().strip().toLowerCase(Locale.ROOT)),cb.equal(x.get("country").get("id"),r.countryId()),cb.equal(x.get("tenantId"),owner)),PageRequest.of(0,2)).getContent());
 }
 private <T extends BaseEntity> UUID unique(List<T> values) {
  if(values.size()>1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Ambiguous academic identity; select an existing entity explicitly");
  return values.isEmpty()?null:values.getFirst().getId();
 }
 private UUID target(Map<String,Object> values,java.util.function.Supplier<UUID> natural) {return values.get("existingEntityId")==null?natural.get():payloads.uuid(values.get("existingEntityId"));}
 private Object merged(ExtractedFact fact,UUID id,String kind,Map<String,Object> values) {
  if(id==null)return payloads.request(fact,values,true);
  var merged=new LinkedHashMap<>(catalogue.get(kind,id));
  values.forEach((key,value)->{if(value!=null)merged.put(key,value);});
  return payloads.request(fact,merged,true);
 }
 private void sameOwner(UUID source,UUID target) {if(!Objects.equals(source,target))throw ResourceAccess.denied();}
 private int order(String kind) {
  return switch(kind) {case "UNIVERSITY" -> 0;case "CAMPUS" -> 1;case "FACULTY" -> 2;case "PROGRAM" -> 3;
   case "ADMISSION_CRITERION" -> 4;case "SCHOLARSHIP" -> 5;case "SCHOLARSHIP_CRITERION" -> 6;default -> 7;};
 }
}
