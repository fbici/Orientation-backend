package com.orientation.orientationapp.knowledge.similarity.service.impl;
import com.orientation.orientationapp.knowledge.similarity.service.SimilarityEngineService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service
public class DefaultSimilarityEngineService implements SimilarityEngineService {
 private ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"No verified embedding model is configured; source-backed TEXT search is available at /knowledge/search");}
 @Override public List<Map<String,Object>> findSimilar(String type,UUID id,int limit){throw unavailable();}
 @Override public double computeSimilarity(String type,UUID a,UUID b){throw unavailable();}
}
