package com.orientation.orientationapp.backoffice.notification_center.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bo_notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String message;

    @Column(length = 100)
    private String userId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(length = 500)
    private String link;

    @Column(nullable = false)
    private Instant createdAt;
}
