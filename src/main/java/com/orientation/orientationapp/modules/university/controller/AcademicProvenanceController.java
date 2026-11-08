package com.orientation.orientationapp.modules.university.controller;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.intelligence.knowledge.KnowledgeGraphService;
import com.orientation.orientationapp.knowledge.repository.KnowledgeNodeRepository;
import com.orientation.orientationapp.docintel.document.service.FactPayloadMapper;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController @RequiredArgsConstructor @Transactional(readOnly=true)
public class AcademicProvenanceController {
 private final com.orientation.orientationapp.modules.university.service.AcademicProvenanceService provenance;
 @GetMapping("/{resource:universities|campuses|faculties|programs|scholarships}/{id}/sources")
 public List<Object> sources(@PathVariable String resource,@PathVariable UUID id) {
  return provenance.sources(resource,id,null,false);
 }
}
