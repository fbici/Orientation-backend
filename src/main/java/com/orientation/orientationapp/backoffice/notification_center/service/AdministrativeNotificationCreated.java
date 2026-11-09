package com.orientation.orientationapp.backoffice.notification_center.service;
import java.util.UUID;
/** Excludes titles, messages and personal account links. */
public record AdministrativeNotificationCreated(UUID notificationId, String userId) {}
