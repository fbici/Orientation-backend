package com.orientation.orientationapp.docintel.document.controller;
import com.orientation.orientationapp.docintel.document.service.*;
import com.orientation.orientationapp.docintel.document.dto.request.*;
import com.orientation.orientationapp.intelligence.pipeline.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import jakarta.validation.Valid;
import java.util.*;
@RestController @RequestMapping("/documents") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class DocumentReviewController {
 private final DocumentReviewService review;
 private final DocumentProcessingPipeline pipeline;
 private final AcademicPublicationService publication;
 private final DocumentPagePreview previews;
 @GetMapping("/{id}/review") public Map<String,Object> review(@PathVariable UUID id){return review.review(id);}
 @GetMapping("/{id}/pages") public List<Map<String,Object>> pages(@PathVariable UUID id){return review.pages(id);}
 @GetMapping("/{id}/extractions") public List<Map<String,Object>> facts(@PathVariable UUID id){return review.facts(id);}
 @PostMapping("/{id}/process") public DocumentProcessingPipeline.PipelineResult process(@PathVariable UUID id){return pipeline.processStoredDocument(id);}
 @PostMapping("/{id}/extractions") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Object> add(@PathVariable UUID id,@Valid @RequestBody AddFactRequest request){return review.add(id,request);}
 @PutMapping("/{id}/extractions/{factId}") public Map<String,Object> decide(@PathVariable UUID id,@PathVariable UUID factId,
  @Valid @RequestBody ReviewFactRequest request){return review.decide(id,factId,request);}
 @PostMapping("/{id}/publish") public Map<String,Object> publish(@PathVariable UUID id,@Valid @RequestBody PublishDocumentRequest request){return publication.publish(id,request);}
 @GetMapping(value="/{id}/pages/{page}/preview",produces=MediaType.IMAGE_PNG_VALUE)
 public ResponseEntity<byte[]> page(@PathVariable UUID id,@PathVariable int page){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_PNG).body(previews.render(id,page));
 }
}
