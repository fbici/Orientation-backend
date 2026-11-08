package com.orientation.orientationapp.modules.auth.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="account_tokens")
public class AccountToken {
    public enum Purpose { VERIFY_EMAIL, RESET_PASSWORD }
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,unique=true,length=64) private String tokenHash;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Purpose purpose;
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private boolean consumed;
}
