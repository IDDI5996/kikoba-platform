package com.kikoba.kikoba_backend.notification.mapper;

import com.kikoba.kikoba_backend.notification.dto.NotificationPreferenceResponse;
import com.kikoba.kikoba_backend.notification.dto.NotificationResponse;
import com.kikoba.kikoba_backend.notification.entity.Notification;
import com.kikoba.kikoba_backend.notification.entity.NotificationPreference;
import com.kikoba.kikoba_backend.notification.enums.NotificationChannel;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .body(notification.getBody())
                .actionUrl(notification.getActionUrl())
                .metadata(notification.getMetadata())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .expiresAt(notification.getExpiresAt())
                .build();
    }

    public NotificationPreferenceResponse toPreferenceResponse(NotificationPreference pref) {
        if (pref == null) return null;

        NotificationType type;
        try {
            type = NotificationType.valueOf(pref.getNotificationType());
        } catch (IllegalArgumentException e) {
            type = null;
        }

        NotificationChannel channel;
        try {
            channel = NotificationChannel.valueOf(pref.getChannel());
        } catch (IllegalArgumentException e) {
            channel = null;
        }

        return NotificationPreferenceResponse.builder()
                .id(pref.getId())
                .notificationType(type)
                .channel(channel)
                .enabled(pref.getEnabled())
                .build();
    }
}