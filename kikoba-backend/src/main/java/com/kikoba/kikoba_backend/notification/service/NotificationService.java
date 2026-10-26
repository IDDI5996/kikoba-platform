package com.kikoba.kikoba_backend.notification.service;

import com.kikoba.kikoba_backend.notification.dto.*;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    // Sending
    NotificationResponse sendNotification(SendNotificationRequest request);
    List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request);

    // Synchronous Convenience methods for internal use
    void notifyUser(UUID userId, NotificationType type, String title, String body);
    void notifyUser(UUID userId, NotificationType type, String title, String body, String actionUrl);
    void notifyUserCritical(UUID userId, NotificationType type, String title, String body);

    // ASYNC convenience methods
    void notifyUserAsync(UUID userId, NotificationType type, String title, String body);
    void notifyUserAsync(UUID userId, NotificationType type, String title, String body, String actionUrl, UUID referenceId);
    void notifyUserAsync(UUID userId, NotificationType type, String title, String body, String actionUrl,
                         String referenceType, UUID referenceId, NotificationPriority priority);


    // Retrieval
    NotificationResponse getNotificationById(UUID id, UUID requesterId);
    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);
    List<NotificationResponse> getUnreadNotifications(UUID userId);
    long getUnreadCount(UUID userId);
    NotificationSummaryResponse getSummary(UUID userId);

    // Actions
    NotificationResponse markAsRead(UUID notificationId, UUID requesterId);
    int markAllAsRead(UUID userId);
    void deleteNotification(UUID notificationId, UUID requesterId);

    // System
    int retryFailedNotifications();
    int cleanupExpiredNotifications();
}