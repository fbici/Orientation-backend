package com.orientation.orientationapp.ai.history;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.*;
public interface DecisionRecordRepository extends JpaRepository<DecisionRecord,UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<DecisionRecord> {
 Page<DecisionRecord> findByCandidateIdOrderByCreatedAtDescIdDesc(UUID candidate,Pageable page);
 Page<DecisionRecord> findByCandidateIdAndKindOrderByCreatedAtDescIdDesc(UUID candidate,String kind,Pageable page);
 Page<DecisionRecord> findByParentIdOrderByCreatedAtAscIdAsc(UUID parent,Pageable page);
}
