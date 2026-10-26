package com.kikoba.kikoba_backend.notification.dto;

import com.kikoba.kikoba_backend.notification.enums.NotificationChannel;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationStatus;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private NotificationPriority priority;
    private String title;
    private String body;
    private String actionUrl;
    private String metadata;
    private String referenceType;
    private UUID referenceId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}