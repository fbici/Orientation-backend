package com.orientation.orientationapp.docintel.document.repository;
import com.orientation.orientationapp.docintel.document.entity.ExtractedFact;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface ExtractedFactRepository extends JpaRepository<ExtractedFact,UUID> {
 List<ExtractedFact> findByDocumentIdOrderByPageNumberAscCreatedAtAsc(UUID documentId);
 Optional<ExtractedFact> findByDocumentVersionIdAndSourceKey(UUID version,String sourceKey);
 List<ExtractedFact> findByPublishedEntityTypeAndPublishedEntityId(String type,UUID entityId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select f from ExtractedFact f where f.id=:id")
 Optional<ExtractedFact> lock(@Param("id") UUID id);
}
