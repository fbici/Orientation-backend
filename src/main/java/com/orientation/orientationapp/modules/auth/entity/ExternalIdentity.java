package com.orientation.orientationapp.modules.auth.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.Instant;
@Entity @Table(name="auth_external_identities") @Getter @Setter @NoArgsConstructor
public class ExternalIdentity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,length=100) private String issuer;
    @Column(nullable=false,length=255) private String subject;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false) private Instant createdAt=Instant.now();
}
