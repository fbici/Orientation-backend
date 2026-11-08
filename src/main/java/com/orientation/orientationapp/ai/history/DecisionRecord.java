package com.orientation.orientationapp.ai.history;
import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity @Table(name="decision_records") @Getter @Setter @NoArgsConstructor
public class DecisionRecord extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="candidate_id",nullable=false)
 private Candidate candidate;
 @Column(nullable=false) private Long candidateVersion;
 @Column(nullable=false,length=32) private String kind;
 @Column(nullable=false) private UUID actorId;
 private UUID parentId;
 @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
 @Column(nullable=false,columnDefinition="jsonb") private String payload;
}
