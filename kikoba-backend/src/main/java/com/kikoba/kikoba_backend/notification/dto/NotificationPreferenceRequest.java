package com.kikoba.kikoba_backend.notification.dto;

import com.kikoba.kikoba_backend.notification.enums.NotificationChannel;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;
}