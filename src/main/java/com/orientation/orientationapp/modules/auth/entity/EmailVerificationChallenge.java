package com.orientation.orientationapp.modules.auth.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="email_verification_challenges") @Getter @Setter @NoArgsConstructor
public class EmailVerificationChallenge {
 @Id private UUID id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false,unique=true) private User user;
 @Column(nullable=false,length=64) private String codeHash;
 @Column(nullable=false) private Instant expiresAt;
 @Column(nullable=false) private Instant lastSentAt;
 private int attempts;
 private int sendCount;
 private boolean consumed;
}
