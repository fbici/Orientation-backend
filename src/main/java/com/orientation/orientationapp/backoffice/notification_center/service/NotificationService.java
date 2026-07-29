package com.orientation.orientationapp.backoffice.notification_center.service;

import com.orientation.orientationapp.backoffice.notification_center.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    Notification create(String type, String title, String message, String userId);
    Page<Notification> getByUser(String userId, Pageable pageable);
    long getUnreadCount(String userId);
    void markAsRead(UUID id);
    void markAllAsRead(String userId);
}
