package com.orientation.orientationapp.modules.university.controller;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.modules.university.dto.CatalogFilter;
import com.orientation.orientationapp.modules.university.dto.CatalogRequests.UniversityWrite;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.*;
@RestController @RequestMapping("/universities") @RequiredArgsConstructor
public class UniversityController {
 private final AcademicCatalogService catalogue;
 @GetMapping public Page<Map<String,Object>> list(@ModelAttribute CatalogFilter filter,
  @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
  return catalogue.list("universities",filter,page,size);
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable UUID id) {return catalogue.get("universities",id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Object> create(@Valid @RequestBody UniversityWrite request) {return catalogue.saveUniversity(null,request);}
 @PutMapping("/{id}") public Map<String,Object> update(@PathVariable UUID id,@Valid @RequestBody UniversityWrite request) {return catalogue.saveUniversity(id,request);}
 @DeleteMapping("/{id}") public Map<String,Object> disable(@PathVariable UUID id) {return catalogue.disable("universities",id);}
}
