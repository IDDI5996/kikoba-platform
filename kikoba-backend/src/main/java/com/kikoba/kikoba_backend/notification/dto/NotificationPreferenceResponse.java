package com.kikoba.kikoba_backend.notification.dto;

import com.kikoba.kikoba_backend.notification.enums.NotificationChannel;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceResponse {
    private UUID id;
    private NotificationType notificationType;
    private NotificationChannel channel;
    private Boolean enabled;
}