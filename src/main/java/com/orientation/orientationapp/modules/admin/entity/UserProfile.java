package com.orientation.orientationapp.modules.admin.entity;

import com.orientation.orientationapp.common.model.BaseEntity;
import com.orientation.orientationapp.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_user_profile_user"))
    private User user;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 200)
    private String jobTitle;

    @Column(length = 200)
    private String department;

    @Column(length = 50)
    private String timezone;

    @Column(length = 10)
    private String language;

    @Column(length = 500)
    private String bio;

    @Column(length = 200)
    private String linkedinUrl;

    @Column(length = 200)
    private String twitterUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean smsNotifications = false;
}
