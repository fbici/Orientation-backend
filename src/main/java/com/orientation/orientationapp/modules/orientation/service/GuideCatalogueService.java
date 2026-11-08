package com.orientation.orientationapp.modules.orientation.service;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.university.service.CatalogAccess;
import com.orientation.orientationapp.docintel.document.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import static com.orientation.orientationapp.modules.university.service.AcademicCatalogService.*;
import static com.orientation.orientationapp.security.service.ResourceAccess.denied;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class GuideCatalogueService {
 private final OrientationGuideRepository guides;
 private final GuideVersionRepository versions;
 private final DocumentVersionRepository sources;
 private final CatalogAccess access;
 public Specification<OrientationGuide> scope() {
  UUID tenant=access.tenant();boolean global=access.global();
  return (r,q,cb)-> {
   var v=q.subquery(UUID.class);var x=v.from(GuideVersion.class);
   v.select(x.get("id")).where(cb.equal(x.get("orientationGuide").get("id"),r.get("id")),
    cb.isNotNull(x.get("documentVersionId")),cb.equal(x.get("status"),com.orientation.orientationapp.common.enums.GuideVersionStatus.ACTIVE));
   return cb.and(cb.isFalse(r.get("deleted")),global?cb.conjunction():tenant==null?cb.isNull(r.get("tenantId")):cb.or(cb.isNull(r.get("tenantId")),cb.equal(r.get("tenantId"),tenant)),cb.exists(v));
  };
 }
 public Page<Map<String,Object>> list(int page,int size,UUID country,UUID year,String search) {
  if(page<0 || size<1 || size>100)throw invalid("Page size must be 1..100");
  return guides.findAll(scope().and((r,q,cb)->cb.and(country==null?cb.conjunction():cb.equal(r.get("country").get("id"),country),
   year==null?cb.conjunction():cb.equal(r.get("academicYear").get("id"),year),
   search==null||search.isBlank()?cb.conjunction():cb.like(cb.lower(r.get("title")),"%"+search.toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%",'\\'))),
   PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"))).map(this::view);
 }
 public OrientationGuide guide(UUID id) {return guides.findOne(scope().and((r,q,cb)->cb.equal(r.get("id"),id))).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Published guide unavailable"));}
 public Map<String,Object> get(UUID id){return view(guide(id));}
 public List<Map<String,Object>> versions(UUID id) {
  guide(id);return versions.findByOrientationGuideId(id).stream().filter(v->v.getDocumentVersionId()!=null && Set.of(com.orientation.orientationapp.common.enums.GuideVersionStatus.ACTIVE,com.orientation.orientationapp.common.enums.GuideVersionStatus.ARCHIVED).contains(v.getStatus()))
   .sorted(Comparator.comparing(GuideVersion::getVersionNumber).reversed()).map(this::version).toList();
 }
 public Map<String,Object> view(OrientationGuide g) {return row("id",g.getId(),"title",g.getTitle(),"publisher",g.getPublisher(),"countryId",g.getCountry().getId(),"country",row("id",g.getCountry().getId(),"name",g.getCountry().getName()),"academicYearId",g.getAcademicYear().getId(),"academicYear",row("id",g.getAcademicYear().getId(),"label",g.getAcademicYear().getLabel()),"tenantId",g.getTenantId(),"versions",versions.findByOrientationGuideId(g.getId()).stream().filter(v->v.getDocumentVersionId()!=null).map(this::version).toList());}
 public Map<String,Object> version(GuideVersion v) {
  var s=sources.findById(v.getDocumentVersionId()).orElseThrow();
  return row("id",v.getId(),"guideId",v.getOrientationGuide().getId(),"versionNumber",v.getVersionNumber(),"versionLabel",v.getVersionLabel(),"status",v.getStatus(),"effectiveDate",v.getEffectiveDate(),"expiryDate",v.getExpiryDate(),"documentId",s.getDocument().getId(),"documentVersionId",s.getId(),"checksum",s.getChecksum(),"sourceDownloadRequiresDocumentPermission",true);
 }
}
