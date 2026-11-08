package com.orientation.orientationapp.modules.orientation.controller;
import com.orientation.orientationapp.modules.orientation.service.GuideCatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.*;
@RestController @RequestMapping("/guides") @RequiredArgsConstructor
public class OrientationGuideController {
 private final GuideCatalogueService catalogue;
 @GetMapping public Page<Map<String,Object>> list(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size,@RequestParam(required=false)UUID countryId,@RequestParam(required=false)UUID academicYearId,@RequestParam(required=false)String search){return catalogue.list(page,size,countryId,academicYearId,search);}
 @GetMapping("/{id}") public Map<String,Object> get(@PathVariable UUID id){return catalogue.get(id);}
 @GetMapping("/{id}/versions") public List<Map<String,Object>> versions(@PathVariable UUID id){return catalogue.versions(id);}
}
