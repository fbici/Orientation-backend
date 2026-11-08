package com.orientation.orientationapp.modules.university.controller;
import com.orientation.orientationapp.modules.university.service.AcademicCatalogService;
import com.orientation.orientationapp.modules.university.dto.CatalogFilter;
import com.orientation.orientationapp.modules.university.dto.CatalogRequests.ProgramWrite;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.*;
@RestController @RequestMapping("/programs") @RequiredArgsConstructor
public class ProgramController {
 private final AcademicCatalogService catalogue;
 @GetMapping public Page<Map<String,Object>> list(@ModelAttribute CatalogFilter filter,
  @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
  return catalogue.list("programs",filter,page,size);
 }
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable UUID id) {return catalogue.get("programs",id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Object> create(@Valid @RequestBody ProgramWrite request) {return catalogue.saveProgram(null,request);}
 @PutMapping("/{id}") public Map<String,Object> update(@PathVariable UUID id,@Valid @RequestBody ProgramWrite request) {return catalogue.saveProgram(id,request);}
 @DeleteMapping("/{id}") public Map<String,Object> disable(@PathVariable UUID id) {return catalogue.disable("programs",id);}
}
