package com.orientation.orientationapp.modules.university.service;
import com.orientation.orientationapp.modules.university.entity.*;
import com.orientation.orientationapp.modules.university.repository.*;
import com.orientation.orientationapp.modules.university.dto.*;
import com.orientation.orientationapp.modules.university.dto.CatalogRequests.*;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.modules.orientation.repository.AcademicYearRepository;
import com.orientation.orientationapp.common.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.time.LocalDate;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AcademicCatalogService {
 private final UniversityRepository universities;
 private final CampusRepository campuses;
 private final FacultyRepository faculties;
 private final ProgramRepository programs;
 private final ScholarshipRepository scholarships;
 private final CountryRepository countries;
 private final CityRepository cities;
 private final AcademicYearRepository years;
 private final CatalogAccess access;
 private final com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository knowledgeNodes;
 private final com.orientation.orientationapp.knowledge.repository.SearchIndexRepository searchIndices;
 private <T> Specification<T> currentEntity(UUID id,String type) {
  return (r,q,cb)-> {
   var active=q.subquery(UUID.class);var v=active.from(com.orientation.orientationapp.modules.orientation.entity.GuideVersion.class);
   active.select(v.get("id")).where(cb.equal(v.get("status"),GuideVersionStatus.ACTIVE));
   return cb.and(cb.equal(r.get("entityId"),id),cb.equal(r.get("entityType"),type),r.get("guideVersionId").in(active));
  };
 }
 private void invalidate(UUID id,String type) {
  if(id==null)return;
  var ns=knowledgeNodes.findAll(currentEntity(id,type));
  ns.forEach(n->n.setActive(false));knowledgeNodes.saveAll(ns);
  var rows=searchIndices.findAll(currentEntity(id,type));
  rows.forEach(n->n.setActive(false));searchIndices.saveAll(rows);
 }

 public static ResponseStatusException invalid(String message) { return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,message); }
 public static <T> T found(Optional<T> value) { return value.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic resource unavailable")); }
 public static Map<String,Object> row(Object... entries) {
  var result=new LinkedHashMap<String,Object>();
  for(int i=0;i<entries.length;i+=2) result.put((String)entries[i],entries[i+1]);return result;
 }
 private Map<String,Object> ref(com.orientation.orientationapp.common.model.BaseEntity value,String name) {
  return value==null?null:row("id",value.getId(),"name",name);
 }
 private String managePermission(String kind) { return kind.equals("programs")?"PROGRAM_UPDATE":kind.equals("scholarships")?"SCHOLARSHIP_MANAGE":"UNIVERSITY_UPDATE"; }
 private boolean verified(Object entity) {
  if(entity instanceof University u)return Boolean.TRUE.equals(u.getVerified());
  if(entity instanceof Campus c)return Boolean.TRUE.equals(c.getVerified()) && verified(c.getUniversity());
  if(entity instanceof Faculty f)return Boolean.TRUE.equals(f.getVerified()) && verified(f.getCampus());
  if(entity instanceof Program p)return Boolean.TRUE.equals(p.getVerified()) && verified(p.getFaculty());
  return entity instanceof Scholarship s && Boolean.TRUE.equals(s.getVerified());
 }
 public Map<String,Object> view(Object entity) {
  var result=viewData(entity);result.put("verificationStatus",verified(entity)?"VALIDATED":"TO_REVIEW");return result;
 }
 private Map<String,Object> viewData(Object entity) {
  if(entity instanceof University u) return row("id",u.getId(),"name",u.getName(),"shortName",u.getShortName(),
   "countryId",u.getCountry().getId(),"countryName",u.getCountry().getName(),"country",ref(u.getCountry(),u.getCountry().getName()),
   "cityId",u.getCity().getId(),"cityName",u.getCity().getName(),"city",ref(u.getCity(),u.getCity().getName()),
   "type",u.getInstitutionType(),"status",u.getStatus(),"active",u.getActive(),"address",u.getAddress(),
   "phone",u.getPhone(),"email",u.getEmail(),"website",u.getWebsite(),"foundedYear",u.getFoundedYear(),
   "description",u.getDescription(),"sourceUrl",u.getSourceUrl(),"tenantId",u.getTenantId());
  if(entity instanceof Campus c) return row("id",c.getId(),"name",c.getName(),"universityId",c.getUniversity().getId(),
   "universityName",c.getUniversity().getName(),"cityId",c.getCity().getId(),"cityName",c.getCity().getName(),
   "address",c.getAddress(),"phone",c.getPhone(),"main",c.getMain(),"active",c.getActive());
  if(entity instanceof Faculty f) return row("id",f.getId(),"name",f.getName(),"code",f.getCode(),"campusId",f.getCampus().getId(),
   "campusName",f.getCampus().getName(),"universityId",f.getCampus().getUniversity().getId(),
   "universityName",f.getCampus().getUniversity().getName(),"description",f.getDescription(),"active",f.getActive());
  if(entity instanceof Program p) return row("id",p.getId(),"name",p.getName(),"code",p.getCode(),"facultyId",p.getFaculty().getId(),
   "facultyName",p.getFaculty().getName(),"campusId",p.getFaculty().getCampus().getId(),
   "universityId",p.getFaculty().getCampus().getUniversity().getId(),"universityName",p.getFaculty().getCampus().getUniversity().getName(),
   "countryId",p.getFaculty().getCampus().getUniversity().getCountry().getId(),"type",p.getType(),"degree",p.getDegree(),
   "duration",p.getDuration(),"language",p.getLanguage(),"studyField",p.getStudyField(),"description",p.getDescription(),
   "prerequisites",p.getPrerequisites(),"tuitionFee",p.getTuitionFee(),"tuitionCurrency",p.getTuitionCurrency(),
   "available",p.getAvailable(),"active",p.getAvailable(),"academicYearId",p.getAcademicYearId(),"website",p.getWebsite(),"sourceUrl",p.getSourceUrl());
  if(entity instanceof Scholarship s) {
   LocalDate now=LocalDate.now();
   Boolean open=com.orientation.orientationapp.modules.scholarship.service.ScholarshipAvailability.open(s,now);
   return row("id",s.getId(),"name",s.getName(),"countryId",s.getCountry().getId(),"countryName",s.getCountry().getName(),
    "academicYearId",s.getAcademicYear().getId(),"academicYear",s.getAcademicYear().getLabel(),"type",s.getType(),
    "status",s.getStatus(),"provider",s.getProvider(),"description",s.getDescription(),"coverage",s.getCoverage(),
    "amount",s.getAmount(),"currency",s.getCurrency(),"duration",s.getDuration(),"deadline",s.getDeadline(),
    "validFrom",s.getValidFrom(),"validUntil",s.getValidUntil(),"applicationUrl",s.getApplicationUrl(),
    "sourceUrl",s.getSourceUrl(),"universityId",s.getUniversityId(),"programId",s.getProgramId(),
    "active",s.getActive(),"openForApplications",open,"tenantId",s.getTenantId());
  }
  throw invalid("Unsupported academic resource");
 }
 private Path<?> universityPath(String kind,jakarta.persistence.criteria.Root<?> root) {
  return switch(kind) { case "campuses" -> root.get("university");case "faculties" -> root.get("campus").get("university");
   case "programs" -> root.get("faculty").get("campus").get("university");default -> root; };
 }
 private <T> Specification<T> filter(String kind,CatalogFilter f) {
  UUID own=access.tenant();boolean global=access.global();
  return (root,query,cb) -> {
   var predicates=new ArrayList<Predicate>();var university=universityPath(kind,root);
   var tenant=university.get("tenantId");
   if(!access.manages(managePermission(kind))) {
    predicates.add(cb.isTrue(root.get("verified")));
    if(!kind.equals("scholarships")) predicates.add(cb.isTrue(university.get("verified")));
    if(kind.equals("faculties")) predicates.add(cb.isTrue(root.get("campus").get("verified")));
    if(kind.equals("programs")) {predicates.add(cb.isTrue(root.get("faculty").get("verified")));predicates.add(cb.isTrue(root.get("faculty").get("campus").get("verified")));}
   }
   if(!global) predicates.add(own==null?cb.isNull(tenant):cb.or(cb.isNull(tenant),cb.equal(tenant,own)));
   if(f.getSearch()!=null && !f.getSearch().isBlank()) {
    if(f.getSearch().length()>200) throw invalid("Search is too long");
    predicates.add(cb.like(cb.lower(root.get("name")),"%"+f.getSearch().strip().toLowerCase(Locale.ROOT).replace("%","\\%").replace("_","\\_")+"%",'\\'));
   }
   if(f.getCountryId()!=null) predicates.add(cb.equal(university.get("country").get("id"),f.getCountryId()));
   if(f.getCityId()!=null && !kind.equals("scholarships")) predicates.add(cb.equal(university.get("city").get("id"),f.getCityId()));
   if(f.getUniversityId()!=null && !kind.equals("scholarships")) predicates.add(cb.equal(university.get("id"),f.getUniversityId()));
   if(f.getCampusId()!=null && kind.equals("faculties")) predicates.add(cb.equal(root.get("campus").get("id"),f.getCampusId()));
   if(f.getCampusId()!=null && kind.equals("programs")) predicates.add(cb.equal(root.get("faculty").get("campus").get("id"),f.getCampusId()));
   if(f.getFacultyId()!=null && kind.equals("programs")) predicates.add(cb.equal(root.get("faculty").get("id"),f.getFacultyId()));
   if(f.getAcademicYearId()!=null) {
    if(kind.equals("programs")) predicates.add(cb.equal(root.get("academicYearId"),f.getAcademicYearId()));
    if(kind.equals("scholarships")) predicates.add(cb.equal(root.get("academicYear").get("id"),f.getAcademicYearId()));
   }
   if(f.getActive()!=null) predicates.add(cb.equal(root.get(kind.equals("programs")?"available":"active"),f.getActive()));
   try {
    if(f.getStatus()!=null && !f.getStatus().isBlank()) {
     if(kind.equals("universities")) predicates.add(cb.equal(root.get("status"),UniversityStatus.valueOf(f.getStatus())));
     else if(kind.equals("scholarships")) predicates.add(cb.equal(root.get("status"),ScholarshipStatus.valueOf(f.getStatus())));
     else throw invalid("Status filter is unsupported for this resource");
    }
    if(f.getType()!=null && !f.getType().isBlank()) predicates.add(cb.equal(root.get(kind.equals("universities")?"institutionType":"type"),
      kind.equals("programs")?ProgramType.valueOf(f.getType()):kind.equals("scholarships")?ScholarshipType.valueOf(f.getType()):f.getType()));
   } catch(IllegalArgumentException ex) { throw invalid("Invalid catalogue enum filter"); }
   if(kind.equals("programs")) {
    if(f.getLanguage()!=null) predicates.add(cb.equal(cb.lower(root.get("language")),f.getLanguage().toLowerCase(Locale.ROOT)));
    if(f.getStudyField()!=null) predicates.add(cb.equal(cb.lower(root.get("studyField")),f.getStudyField().toLowerCase(Locale.ROOT)));
   }
   return cb.and(predicates.toArray(Predicate[]::new));
  };
 }
 public Page<Map<String,Object>> list(String kind,CatalogFilter filter,int page,int size) {
  if(page<0 || size<1 || size>100) throw invalid("Pagination requires page >= 0 and size between 1 and 100");
  Pageable p=PageRequest.of(page,size,Sort.by("name").ascending());
  return switch(kind) {
   case "universities" -> universities.findAll(filter(kind,filter),p).map(this::view);
   case "campuses" -> campuses.findAll(filter(kind,filter),p).map(this::view);
   case "faculties" -> faculties.findAll(filter(kind,filter),p).map(this::view);
   case "programs" -> programs.findAll(filter(kind,filter),p).map(this::view);
   case "scholarships" -> scholarships.findAll(filter(kind,filter),p).map(this::view);
   default -> throw invalid("Unsupported resource");
  };
 }
 public University university(UUID id) { var u=found(universities.findById(id));access.read(u.getTenantId());return u; }
 public Campus campus(UUID id) { var c=found(campuses.findById(id));access.read(c.getUniversity().getTenantId());return c; }
 public Faculty faculty(UUID id) { var f=found(faculties.findById(id));access.read(f.getCampus().getUniversity().getTenantId());return f; }
 public Program program(UUID id) { var p=found(programs.findById(id));access.read(p.getFaculty().getCampus().getUniversity().getTenantId());return p; }
 public Scholarship scholarship(UUID id) {var s=found(scholarships.findById(id));access.read(s.getTenantId());return s;}
 public Map<String,Object> get(String kind,UUID id) {
  Object entity=switch(kind) {case "universities" -> university(id);case "campuses" -> campus(id);
   case "faculties" -> faculty(id);case "programs" -> program(id);case "scholarships" -> scholarship(id);default -> throw invalid("Unsupported resource");};
  if(!verified(entity) && !access.manages(managePermission(kind)))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Academic resource is not published");
  return view(entity);
 }
 private String url(String value) {
  if(value==null || value.isBlank()) return null;
  try { var uri=java.net.URI.create(value);if(!Set.of("https","http").contains(uri.getScheme()) || uri.getHost()==null) throw new IllegalArgumentException(); }
  catch(IllegalArgumentException ex) {throw invalid("URLs must use HTTP or HTTPS");}
  return value;
 }
 @Transactional public Map<String,Object> saveUniversity(UUID id,UniversityWrite r) {return saveUniversityOwned(id,r,null);}
 @Transactional public Map<String,Object> saveUniversityOwned(UUID id,UniversityWrite r,UUID owner) {
  String permission=id==null?"UNIVERSITY_CREATE":"UNIVERSITY_UPDATE";
  University u=id==null?new University():university(id);
  if(id==null){u.setTenantId(owner==null?access.create(permission):owner);access.write(u.getTenantId(),permission);}else access.write(u.getTenantId(),permission);
  var country=found(countries.findById(r.countryId()));var city=found(cities.findById(r.cityId()));
  if(!city.getCountry().getId().equals(country.getId())) throw invalid("City does not belong to the selected country");
  if(r.type()!=null && !Set.of("PUBLIC","PRIVATE","OTHER").contains(r.type())) throw invalid("University type must be PUBLIC, PRIVATE or OTHER");
  u.setName(r.name().strip());u.setShortName(r.shortName());u.setCountry(country);u.setCity(city);u.setInstitutionType(r.type());
  u.setStatus(r.status()==null?UniversityStatus.PENDING:r.status());u.setActive(u.getStatus()==UniversityStatus.ACTIVE);
  u.setAddress(r.address());u.setPhone(r.phone());u.setEmail(r.email());u.setWebsite(url(r.website()));u.setFoundedYear(r.foundedYear());
  u.setDescription(r.description());u.setSourceUrl(url(r.sourceUrl()));
  u.setVerified(false);invalidate(id,"UNIVERSITY");
  return view(universities.saveAndFlush(u));
 }
 @Transactional public Map<String,Object> saveCampus(UUID id,CampusWrite r) {
  Campus c=id==null?new Campus():campus(id);
  if(id!=null) access.write(c.getUniversity().getTenantId(),"UNIVERSITY_UPDATE");
  var u=university(r.universityId());access.write(u.getTenantId(),"UNIVERSITY_UPDATE");
  var city=found(cities.findById(r.cityId()));
  if(!city.getCountry().getId().equals(u.getCountry().getId())) throw invalid("Campus city must belong to the university country");
  c.setUniversity(u);c.setCity(city);c.setName(r.name().strip());c.setAddress(r.address());c.setPhone(r.phone());
  c.setMain(Boolean.TRUE.equals(r.main()));c.setActive(r.active()==null||r.active());
  c.setVerified(false);invalidate(id,"CAMPUS");
  return view(campuses.saveAndFlush(c));
 }
 @Transactional public Map<String,Object> saveFaculty(UUID id,FacultyWrite r) {
  Faculty f=id==null?new Faculty():faculty(id);
  if(id!=null) access.write(f.getCampus().getUniversity().getTenantId(),"UNIVERSITY_UPDATE");
  var c=campus(r.campusId());access.write(c.getUniversity().getTenantId(),"UNIVERSITY_UPDATE");
  f.setCampus(c);f.setName(r.name().strip());f.setCode(r.code());f.setDescription(r.description());f.setActive(r.active()==null||r.active());
  f.setVerified(false);invalidate(id,"FACULTY");
  return view(faculties.saveAndFlush(f));
 }
 @Transactional public Map<String,Object> saveProgram(UUID id,ProgramWrite r) {
  String permission=id==null?"PROGRAM_CREATE":"PROGRAM_UPDATE";
  Program p=id==null?new Program():program(id);
  if(id!=null)access.write(p.getFaculty().getCampus().getUniversity().getTenantId(),permission);
  var f=faculty(r.facultyId());access.write(f.getCampus().getUniversity().getTenantId(),permission);
  if(r.academicYearId()!=null) found(years.findById(r.academicYearId()));
  if(r.tuitionFee()!=null && r.tuitionCurrency()==null)throw invalid("Tuition currency is required with a fee");
  p.setName(r.name().strip());p.setFaculty(f);p.setCode(r.code());p.setType(r.type());p.setDegree(r.degree());p.setDuration(r.duration());
  p.setLanguage(r.language());p.setStudyField(r.studyField());p.setDescription(r.description());p.setPrerequisites(r.prerequisites());
  p.setTuitionFee(r.tuitionFee());p.setTuitionCurrency(r.tuitionCurrency());p.setAvailable(r.available()==null?(id==null?false:p.getAvailable()):r.available());
  p.setAcademicYearId(r.academicYearId());p.setWebsite(url(r.website()));p.setSourceUrl(url(r.sourceUrl()));
  p.setVerified(false);invalidate(id,"PROGRAM");
  return view(programs.saveAndFlush(p));
 }
 @Transactional public Map<String,Object> saveScholarship(UUID id,ScholarshipWrite r) {return saveScholarshipOwned(id,r,null);}
 @Transactional public Map<String,Object> saveScholarshipOwned(UUID id,ScholarshipWrite r,UUID owner) {
  Scholarship s=id==null?new Scholarship():scholarship(id);
  if(id==null){s.setTenantId(owner==null?access.create("SCHOLARSHIP_MANAGE"):owner);access.write(s.getTenantId(),"SCHOLARSHIP_MANAGE");}else access.write(s.getTenantId(),"SCHOLARSHIP_MANAGE");
  if(r.validFrom()!=null && r.validUntil()!=null && r.validUntil().isBefore(r.validFrom())) throw invalid("Validity dates are reversed");
  if(r.amount()!=null && r.currency()==null)throw invalid("Currency is required with an amount");
  if(r.universityId()!=null) university(r.universityId());if(r.programId()!=null)program(r.programId());
  if(r.programId()!=null && r.universityId()!=null && !program(r.programId()).getFaculty().getCampus().getUniversity().getId().equals(r.universityId()))
   throw invalid("Scholarship programme does not belong to the selected university");
  s.setCountry(found(countries.findById(r.countryId())));s.setAcademicYear(found(years.findById(r.academicYearId())));
  s.setName(r.name().strip());s.setType(r.type());s.setStatus(r.status());s.setProvider(r.provider());s.setDescription(r.description());
  s.setCoverage(r.coverage());s.setAmount(r.amount());s.setCurrency(r.currency());s.setDuration(r.duration());s.setDeadline(r.deadline());
  s.setValidFrom(r.validFrom());s.setValidUntil(r.validUntil());s.setApplicationUrl(url(r.applicationUrl()));s.setSourceUrl(url(r.sourceUrl()));
  s.setUniversityId(r.universityId());s.setProgramId(r.programId());s.setActive(r.active()==null||r.active());
  s.setVerified(false);invalidate(id,"SCHOLARSHIP");
  return view(scholarships.saveAndFlush(s));
 }
 @Transactional public Map<String,Object> disable(String kind,UUID id) {
  switch(kind) {
   case "universities" -> { var u=university(id);access.write(u.getTenantId(),"UNIVERSITY_DELETE");u.setActive(false);u.setStatus(UniversityStatus.INACTIVE);universities.save(u);}
   case "campuses" -> {var c=campus(id);access.write(c.getUniversity().getTenantId(),"UNIVERSITY_UPDATE");c.setActive(false);campuses.save(c);}
   case "faculties" -> {var f=faculty(id);access.write(f.getCampus().getUniversity().getTenantId(),"UNIVERSITY_UPDATE");f.setActive(false);faculties.save(f);}
   case "programs" -> {var p=program(id);access.write(p.getFaculty().getCampus().getUniversity().getTenantId(),"PROGRAM_DELETE");p.setAvailable(false);programs.save(p);}
   case "scholarships" -> {var s=scholarship(id);access.write(s.getTenantId(),"SCHOLARSHIP_MANAGE");s.setActive(false);s.setStatus(ScholarshipStatus.CLOSED);scholarships.save(s);}
   default -> throw invalid("Unsupported resource");
  }
  invalidate(id,switch(kind){case "universities"->"UNIVERSITY";case "campuses"->"CAMPUS";case "faculties"->"FACULTY";case "programs"->"PROGRAM";default->"SCHOLARSHIP";});
  return get(kind,id);
 }
}
