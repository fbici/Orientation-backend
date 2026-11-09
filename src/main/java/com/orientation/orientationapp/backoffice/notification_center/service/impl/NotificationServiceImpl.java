package com.orientation.orientationapp.backoffice.notification_center.service.impl;

import com.orientation.orientationapp.backoffice.notification_center.entity.Notification;
import com.orientation.orientationapp.backoffice.notification_center.repository.NotificationRepository;
import com.orientation.orientationapp.backoffice.notification_center.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final org.springframework.context.ApplicationEventPublisher events;
    private final com.orientation.orientationapp.security.service.ResourceAccess access;

    @Override
    @Transactional
    public Notification create(String type, String title, String message, String userId) {
        Notification notification = Notification.builder()
                .type(type)
                .title(title)
                .message(message)
                .userId(userId)
                .read(false)
                .createdAt(Instant.now())
                .build();
        Notification saved = notificationRepository.save(notification);
        events.publishEvent(new com.orientation.orientationapp.backoffice.notification_center.service.AdministrativeNotificationCreated(saved.getId(), userId));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getByUser(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(access.personalUser(userId), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(access.personalUser(userId));
    }

    @Override
    @Transactional
    public void markAsRead(UUID id) {
        var notification=notificationRepository.findById(id)
            .orElseThrow(com.orientation.orientationapp.security.service.ResourceAccess::denied);
        if(notification.getUserId()==null) throw com.orientation.orientationapp.security.service.ResourceAccess.denied();
        access.personalUser(notification.getUserId());
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        Page<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(access.personalUser(userId), Pageable.unpaged());
        unread.getContent().forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
