package com.orientation.orientationapp.modules.university.controller;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.modules.university.dto.CatalogFilter;
import com.orientation.orientationapp.modules.university.dto.CatalogRequests.CampusWrite;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.*;
@RestController @RequestMapping("/campuses") @RequiredArgsConstructor
public class CampusController {
 private final AcademicCatalogService catalogue;
 @GetMapping public Page<Map<String,Object>> list(@ModelAttribute CatalogFilter filter,
  @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
  return catalogue.list("campuses",filter,page,size);
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable UUID id) {return catalogue.get("campuses",id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Object> create(@Valid @RequestBody CampusWrite request) {return catalogue.saveCampus(null,request);}
 @PutMapping("/{id}") public Map<String,Object> update(@PathVariable UUID id,@Valid @RequestBody CampusWrite request) {return catalogue.saveCampus(id,request);}
 @DeleteMapping("/{id}") public Map<String,Object> disable(@PathVariable UUID id) {return catalogue.disable("campuses",id);}
}
