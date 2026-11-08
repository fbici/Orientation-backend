package com.orientation.orientationapp.modules.university.controller;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.modules.university.dto.CatalogFilter;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import com.orientation.orientationapp.modules.orientation.service.AdmissionRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@RestController @RequestMapping("/benin/orientation") @RequiredArgsConstructor
public class BeninOrientationController {
 private final AcademicCatalogService catalogue;private final CountryRepository countries;private final AdmissionRuleService rules;
 private Optional<UUID> country(){return countries.findByCode("BEN").or(()->countries.findByCode("BJ")).map(c->c.getId());}
 @GetMapping("/filieres") public List<Map<String,Object>> programs(@RequestParam(required=false)String serie,@RequestParam(required=false)String university){
  var country=country();if(country.isEmpty())return List.of();var f=new CatalogFilter();f.setCountryId(country.get());f.setActive(true);
  var rows=catalogue.list("programs",f,0,100).getContent();
  return rows.stream().filter(p->"VALIDATED".equals(p.get("verificationStatus"))).filter(p->university==null || String.valueOf(p.get("universityName")).toLowerCase(Locale.ROOT).contains(university.toLowerCase(Locale.ROOT)))
   .filter(p->serie==null || rules.list((UUID)p.get("id"),null,0,100).stream().anyMatch(r->"bacType".equals(r.get("fieldName")) && Set.of("IN_LIST","EQUAL").contains(String.valueOf(r.get("operator"))) && Arrays.stream(String.valueOf(r.get("stringValue")).split("[,;|]")).anyMatch(v->v.strip().equalsIgnoreCase(serie)))).toList();
 }
 @GetMapping("/universites")public List<Map<String,Object>> universities(){var country=country();if(country.isEmpty())return List.of();var f=new CatalogFilter();f.setCountryId(country.get());return catalogue.list("universities",f,0,100).stream().filter(p->"VALIDATED".equals(p.get("verificationStatus"))).toList();}
 @GetMapping("/series")public List<Object> series(){return programs(null,null).stream().flatMap(p->rules.list((UUID)p.get("id"),null,0,100).stream()).filter(r->"bacType".equals(r.get("fieldName"))).map(r->r.get("stringValue")).filter(Objects::nonNull).distinct().toList();}
 @PostMapping("/simulate")public void simulate(){throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,"Candidate recommendation validation belongs to F3; use published source-backed catalogue and admission criteria");}
}
